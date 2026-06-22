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

import com.example.proyect_final.domain.repository.AuthRepository
import com.example.proyect_final.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore

sealed interface DetailState {
    object Loading : DetailState
    data class Success(val product: Product) : DetailState
    data class Error(val message: String) : DetailState
}

data class ProductReview(
    val userId: String = "",
    val userName: String = "",
    val comment: String = "",
    val rating: Double = 5.0,
    val timestamp: Long = System.currentTimeMillis()
)

class ProductDetailViewModel(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val productId: String
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _currentUserState = MutableStateFlow<User?>(null)
    val currentUserState: StateFlow<User?> = _currentUserState.asStateFlow()

    private val _uiState = MutableStateFlow<DetailState>(DetailState.Loading)
    val uiState: StateFlow<DetailState> = _uiState.asStateFlow()

    private val _recommendedProducts = MutableStateFlow<List<Product>>(emptyList())
    val recommendedProducts: StateFlow<List<Product>> = _recommendedProducts.asStateFlow()

    private val _complementaryAccessories = MutableStateFlow<List<Product>>(emptyList())
    val complementaryAccessories: StateFlow<List<Product>> = _complementaryAccessories.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _reviews = MutableStateFlow<List<ProductReview>>(emptyList())
    val reviews: StateFlow<List<ProductReview>> = _reviews.asStateFlow()

    private var favoriteListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        getProduct()
        observeCurrentUser()
        observeFavoriteStatus()
        observeReviews()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _currentUserState.value = user
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            _currentUserState.collect { user ->
                favoriteListenerRegistration?.remove()
                val userId = user?.uid
                if (userId != null) {
                    favoriteListenerRegistration = firestore.collection("users").document(userId)
                        .collection("favorites").document(productId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            val isFav = snapshot?.getBoolean("isFavorite") ?: false
                            _isFavorite.value = isFav
                        }
                } else {
                    _isFavorite.value = false
                }
            }
        }
    }

    private fun observeReviews() {
        firestore.collection("reviews")
            .whereEqualTo("productId", productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val userId = doc.getString("userId") ?: ""
                    val userName = doc.getString("userName") ?: ""
                    val comment = doc.getString("comment") ?: ""
                    val rating = doc.getDouble("rating") ?: 5.0
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    ProductReview(userId, userName, comment, rating, timestamp)
                } ?: emptyList()
                
                // Merge with any local reviews that failed to save to Firestore
                val currentLocalOnly = _reviews.value.filter { local ->
                    list.none { it.userId == local.userId && it.comment == local.comment }
                }
                _reviews.value = (list + currentLocalOnly).sortedByDescending { it.timestamp }
            }
    }

    fun toggleFavorite() {
        val userId = _currentUserState.value?.uid ?: return
        val nextFavState = !_isFavorite.value
        
        val data = mapOf(
            "productId" to productId,
            "isFavorite" to nextFavState,
            "timestamp" to System.currentTimeMillis()
        )
        
        firestore.collection("users").document(userId)
            .collection("favorites").document(productId)
            .set(data)
            .addOnSuccessListener {
                _isFavorite.value = nextFavState
                
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

    fun addReview(rating: Double, comment: String, onComplete: (Result<Unit>) -> Unit) {
        val currentUser = _currentUserState.value
        if (currentUser == null) {
            onComplete(Result.failure(Exception("Debe iniciar sesión para dejar una reseña")))
            return
        }
        val userId = currentUser.uid
        val email = currentUser.email ?: "Anónimo"
        val userName = email.substringBefore("@")
        
        val timestamp = System.currentTimeMillis()
        val reviewData = mapOf(
            "productId" to productId,
            "userId" to userId,
            "userName" to userName,
            "comment" to comment,
            "rating" to rating,
            "timestamp" to timestamp
        )
        
        val docId = "${userId}_${productId}"
        
        firestore.collection("reviews").document(docId)
            .set(reviewData)
            .addOnSuccessListener {
                firestore.collection("users").document(userId)
                    .collection("reviews").document(productId)
                    .set(reviewData)
                    .addOnSuccessListener {
                        // Log interaction in Firestore
                        val interaction = mapOf(
                            "userId" to userId,
                            "productId" to productId,
                            "action" to "comment",
                            "comment" to comment,
                            "rating" to rating,
                            "timestamp" to System.currentTimeMillis()
                        )
                        firestore.collection("interactions").add(interaction)
                            .addOnCompleteListener {
                                onComplete(Result.success(Unit))
                            }
                    }
                    .addOnFailureListener {
                        onComplete(Result.success(Unit))
                    }
            }
            .addOnFailureListener { e ->
                // Guardar localmente si falla Firestore (offline/reglas/etc)
                android.util.Log.d("ProductDetailViewModel", "Firestore error, agregando reseña localmente", e)
                val localReview = ProductReview(userId, userName, comment, rating, timestamp)
                _reviews.value = (listOf(localReview) + _reviews.value).distinctBy { it.userId + "_" + it.timestamp }.sortedByDescending { it.timestamp }
                onComplete(Result.success(Unit))
            }
    }

    fun getProduct() {
        viewModelScope.launch {
            _uiState.value = DetailState.Loading
            productRepository.getProductById(productId)
                .onSuccess { product ->
                    _uiState.value = DetailState.Success(product)
                    productRepository.getProducts()
                        .onSuccess { allProducts ->
                            val filtered = allProducts
                                .filter { it.id != product.id && it.category.equals(product.category, ignoreCase = true) }
                                .take(4)
                            _recommendedProducts.value = if (filtered.isNotEmpty()) filtered else allProducts.filter { it.id != product.id }.take(4)

                            // Load accessories matching the gender
                            val targetGender = product.gender
                            val accessories = allProducts.filter {
                                it.category.equals("Accesorios", ignoreCase = true) &&
                                (targetGender.equals("Unisex", ignoreCase = true) ||
                                 it.gender.equals("Unisex", ignoreCase = true) ||
                                 it.gender.equals(targetGender, ignoreCase = true))
                            }.take(4)

                            if (accessories.size >= 2) {
                                _complementaryAccessories.value = accessories
                            } else {
                                // Fallback high-quality accessories matching gender
                                val fallbackList = when {
                                    targetGender.equals("Hombre", ignoreCase = true) -> listOf(
                                        Product(
                                            id = "mock_acc_h1",
                                            title = "Billetera Minimalista",
                                            price = 120.0,
                                            description = "Billetera de cuero premium.",
                                            category = "Accesorios",
                                            image = "https://images.unsplash.com/photo-1627123424574-724758594e93?q=80&w=1000",
                                            brand = "Elara",
                                            stock = 10,
                                            sizes = emptyList(),
                                            gender = "Hombre"
                                        ),
                                        Product(
                                            id = "mock_acc_h2",
                                            title = "Reloj Deportivo Smart",
                                            price = 350.0,
                                            description = "Reloj deportivo inteligente.",
                                            category = "Accesorios",
                                            image = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?q=80&w=1000",
                                            brand = "Elara",
                                            stock = 10,
                                            sizes = emptyList(),
                                            gender = "Hombre"
                                        )
                                    )
                                    targetGender.equals("Mujer", ignoreCase = true) -> listOf(
                                        Product(
                                            id = "mock_acc_m1",
                                            title = "Bolso Prism Leather",
                                            price = 280.0,
                                            description = "Bolso de cuero elegante.",
                                            category = "Accesorios",
                                            image = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?q=80&w=1000",
                                            brand = "Elara",
                                            stock = 10,
                                            sizes = emptyList(),
                                            gender = "Mujer"
                                        ),
                                        Product(
                                            id = "mock_acc_m2",
                                            title = "Aros Esculturales",
                                            price = 145.0,
                                            description = "Aros de plata de alta calidad.",
                                            category = "Accesorios",
                                            image = "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?q=80&w=1000",
                                            brand = "Elara",
                                            stock = 10,
                                            sizes = emptyList(),
                                            gender = "Mujer"
                                        )
                                    )
                                    else -> listOf(
                                        Product(
                                            id = "mock_acc_u1",
                                            title = "Mochila Urbana Tech",
                                            price = 240.0,
                                            description = "Mochila resistente al agua.",
                                            category = "Accesorios",
                                            image = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?q=80&w=1000",
                                            brand = "Elara",
                                            stock = 10,
                                            sizes = emptyList(),
                                            gender = "Unisex"
                                        ),
                                        Product(
                                            id = "mock_acc_u2",
                                            title = "Lentes Aviador Clásicos",
                                            price = 190.0,
                                            description = "Lentes con protección UV400.",
                                            category = "Accesorios",
                                            image = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?q=80&w=1000",
                                            brand = "Elara",
                                            stock = 10,
                                            sizes = emptyList(),
                                            gender = "Unisex"
                                        )
                                    )
                                }
                                _complementaryAccessories.value = fallbackList
                            }
                        }
                }
                .onFailure { error ->
                    _uiState.value = DetailState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        favoriteListenerRegistration?.remove()
    }

    companion object {
        fun provideFactory(productId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StyleGenApplication)
                val productRepository = application.container.productRepository
                val authRepository = application.container.authRepository
                ProductDetailViewModel(
                    productRepository = productRepository,
                    authRepository = authRepository,
                    productId = productId
                )
            }
        }
    }
}
