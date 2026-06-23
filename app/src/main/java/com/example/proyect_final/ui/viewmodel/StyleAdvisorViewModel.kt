package com.example.proyect_final.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_final.StyleGenApplication
import com.example.proyect_final.data.local.StyleAdviceEntity
import com.example.proyect_final.domain.model.Product
import com.example.proyect_final.domain.repository.ProductRepository
import com.example.proyect_final.domain.repository.StyleAdvisorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdvisorState {
    object Idle : AdvisorState
    object Loading : AdvisorState
    data class Success(val advice: String, val alternativeProducts: List<Product> = emptyList()) : AdvisorState
    data class Error(val message: String) : AdvisorState
}

class StyleAdvisorViewModel(
    private val productRepository: ProductRepository,
    private val advisorRepository: StyleAdvisorRepository,
    private val productId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdvisorState>(AdvisorState.Idle)
    val uiState: StateFlow<AdvisorState> = _uiState.asStateFlow()

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            if (productId != "0" && productId.isNotEmpty()) {
                productRepository.getProductById(productId).onSuccess {
                    _product.value = it
                }
            }
        }
    }

    fun getAdvice(userImage: Bitmap) {
        val currentProduct = _product.value ?: return
        viewModelScope.launch {
            _uiState.value = AdvisorState.Loading
            val isAccessory = currentProduct.category.equals("Accesorios", ignoreCase = true)
            advisorRepository.getStyleAdvice(currentProduct.image, userImage, isAccessory)
                .onSuccess { result ->
                    val isGood = result.adviceText.contains("buena", ignoreCase = true) || 
                                 result.adviceText.contains("excelente", ignoreCase = true)
                    
                    var alternatives = emptyList<Product>()
                    if (!isGood) {
                        productRepository.getProducts().onSuccess { allProducts ->
                            alternatives = allProducts.filter { 
                                it.category.equals(currentProduct.category, ignoreCase = true) && 
                                it.id != currentProduct.id 
                            }.take(4)
                        }
                    }

                    _uiState.value = AdvisorState.Success(result.adviceText, alternatives)
                    
                    // Save to Room with the path returned by the repository
                    advisorRepository.saveAdvice(
                        StyleAdviceEntity(
                            shopProductId = currentProduct.id,
                            shopProductImage = currentProduct.image,
                            userProductImage = result.userImagePath,
                            adviceText = result.adviceText,
                            isGoodMatch = isGood
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = AdvisorState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    fun resetAdvisorState() {
        _uiState.value = AdvisorState.Idle
    }

    companion object {
        fun provideFactory(productId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StyleGenApplication)
                StyleAdvisorViewModel(
                    productRepository = application.container.productRepository,
                    advisorRepository = application.container.styleAdvisorRepository,
                    productId = productId
                )
            }
        }
    }
}
