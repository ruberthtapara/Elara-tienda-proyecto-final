package com.example.proyect_final.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_final.StyleGenApplication
import com.example.proyect_final.domain.model.Product
import com.example.proyect_final.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.content
import com.example.proyect_final.BuildConfig
import com.example.proyect_final.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

sealed interface HomeState {
    object Loading : HomeState
    data class Success(val products: List<Product>) : HomeState
    data class Error(val message: String) : HomeState
}

class HomeViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private var favoritesListener: ListenerRegistration? = null
    private var currentUserId: String? = null

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _aiSearchResult = MutableStateFlow<List<Product>?>(null)
    val aiSearchResult: StateFlow<List<Product>?> = _aiSearchResult.asStateFlow()

    private val _aiSearchLoading = MutableStateFlow(false)
    val aiSearchLoading: StateFlow<Boolean> = _aiSearchLoading.asStateFlow()

    private val _aiSearchError = MutableStateFlow<String?>(null)
    val aiSearchError: StateFlow<String?> = _aiSearchError.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        getProducts()
        observeUserFavorites()
    }

    private fun observeUserFavorites() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                favoritesListener?.remove()
                val uid = user?.uid
                currentUserId = uid
                if (uid != null) {
                    favoritesListener = firestore.collection("users").document(uid)
                        .collection("favorites")
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            val favs = snapshot?.documents?.filter { 
                                it.getBoolean("isFavorite") == true 
                            }?.map { it.id }?.toSet() ?: emptySet()
                            _favoriteIds.value = favs
                        }
                } else {
                    _favoriteIds.value = emptySet()
                }
            }
        }
    }

    fun toggleFavorite(productId: String) {
        val userId = currentUserId ?: return
        val isFav = _favoriteIds.value.contains(productId)
        val nextFavState = !isFav
        val data = mapOf(
            "productId" to productId,
            "isFavorite" to nextFavState,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("users").document(userId)
            .collection("favorites").document(productId)
            .set(data)
            .addOnSuccessListener {
                // Log interaction in Firestore
                val interaction = mapOf(
                    "userId" to userId,
                    "productId" to productId,
                    "action" to if (nextFavState) "like" else "unlike",
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("interactions").add(interaction)
            }
    }

    override fun onCleared() {
        super.onCleared()
        favoritesListener?.remove()
    }

    fun getProducts() {
        viewModelScope.launch {
            _uiState.value = HomeState.Loading
            // Solo cargamos 15 productos para la pantalla de inicio para que sea súper rápido
            productRepository.getProducts(limit = 15)
                .onSuccess { products ->
                    _uiState.value = HomeState.Success(products)
                }
                .onFailure { error ->
                    _uiState.value = HomeState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    fun fetchAllProducts() {
        viewModelScope.launch {
            // Si ya tenemos productos, no mostramos loading de pantalla completa para no molestar
            productRepository.getProducts(limit = null)
                .onSuccess { products ->
                    _uiState.value = HomeState.Success(products)
                }
        }
    }

    fun searchProductsWithAI(query: String) {
        if (query.trim().isEmpty()) return
        viewModelScope.launch {
            _aiSearchLoading.value = true
            _aiSearchError.value = null
            
            productRepository.getProducts(limit = null).onSuccess { allProducts ->
                try {
                    if (BuildConfig.GEMINI_API_KEY.isEmpty() || BuildConfig.GEMINI_API_KEY.contains("tu_clave")) {
                        throw Exception("La API Key de Gemini no está configurada.")
                    }
                    
                    val catalogText = allProducts.joinToString("\n") { product ->
                        "ID: ${product.id} | Título: ${product.title} | Categoría: ${product.category} | Descripción: ${product.description}"
                    }
                    
                    val prompt = """
                        Eres un asistente inteligente de búsqueda de moda en la tienda Elara.
                        El usuario está buscando ropa con esta descripción: "$query"
                        
                        Aquí tienes el catálogo de la tienda:
                        $catalogText
                        
                        Por favor, selecciona hasta 4 productos del catálogo que mejor coincidan con lo que busca el usuario.
                        Sé inteligente al emparejar sinónimos o conceptos de moda (por ejemplo, "sastre elegante" combina con un saco o blazer elegante; "ropa para frío" con poleras o abrigos).
                        
                        Responde ESTRICTAMENTE en este formato: una lista de IDs separados por comas, en orden de relevancia (por ejemplo: "1, 3, 5").
                        Si absolutamente nada coincide, responde "NONE". No escribas nada más, solo los IDs o la palabra "NONE".
                    """.trimIndent()
                    
                    val genConfig = generationConfig {
                        maxOutputTokens = 30
                        temperature = 0.2f
                    }
                    
                    var retryCount = 0
                    val maxRetries = 2
                    var responseText: String? = null
                    
                    while (retryCount < maxRetries && responseText == null) {
                        try {
                            val modelName = if (retryCount > 0) "gemini-3.1-flash-lite" else "gemini-3.5-flash"
                            val model = GenerativeModel(
                                modelName = modelName,
                                apiKey = BuildConfig.GEMINI_API_KEY,
                                generationConfig = genConfig
                            )
                            val response = model.generateContent(prompt)
                            responseText = response.text
                        } catch (e: Exception) {
                            retryCount++
                            if (retryCount >= maxRetries) throw e
                        }
                    }
                    
                    val cleanResponse = responseText?.trim() ?: "NONE"
                    if (cleanResponse == "NONE" || cleanResponse.isEmpty()) {
                        _aiSearchResult.value = emptyList()
                    } else {
                        val matchedIds = cleanResponse.split(",").map { it.trim() }
                        val results = allProducts.filter { product ->
                            matchedIds.contains(product.id)
                        }
                        _aiSearchResult.value = results
                    }
                } catch (e: Exception) {
                    // Fallback a búsqueda de palabras clave tradicional
                    val keywords = query.lowercase().split(" ")
                    val filtered = allProducts.filter { product ->
                        keywords.any { word ->
                            product.title.lowercase().contains(word) || 
                            product.description.lowercase().contains(word) || 
                            product.category.lowercase().contains(word)
                        }
                    }.take(4)
                    _aiSearchResult.value = filtered
                } finally {
                    _aiSearchLoading.value = false
                }
            }.onFailure {
                _aiSearchError.value = it.message ?: "Error al cargar catálogo"
                _aiSearchLoading.value = false
            }
        }
    }

    fun clearAiSearch() {
        _aiSearchResult.value = null
        _aiSearchError.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StyleGenApplication)
                val productRepository = application.container.productRepository
                val authRepository = application.container.authRepository
                HomeViewModel(
                    productRepository = productRepository,
                    authRepository = authRepository
                )
            }
        }
    }
}
