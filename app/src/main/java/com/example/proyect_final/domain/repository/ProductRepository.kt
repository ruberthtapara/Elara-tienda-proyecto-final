package com.example.proyect_final.domain.repository

import com.example.proyect_final.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(limit: Long? = null): Result<List<Product>>
    suspend fun getProductById(id: String): Result<Product>
}
