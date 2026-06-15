package com.example.proyect_final.data.repository

import android.content.Context
import com.example.proyect_final.data.remote.ElaraApiService
import com.example.proyect_final.data.remote.toDomain
import com.example.proyect_final.domain.model.Product
import com.example.proyect_final.domain.repository.ProductRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class ProductRepositoryImpl(
    private val apiService: ElaraApiService,
    private val context: Context
) : ProductRepository {

    private val json = Json { ignoreUnknownKeys = true }
    
    // Caché en memoria para almacenar todos los productos cargados y evitar peticiones repetidas o fallos de red
    private val productsMemoryCache = ConcurrentHashMap<String, Product>()
    
    private val cachedProducts: List<Product> by lazy {
        try {
            val jsonString = context.assets.open("virtual_catalog.json").bufferedReader().use { it.readText() }
            val localList = json.decodeFromString<List<com.example.proyect_final.data.remote.ProductDto>>(jsonString)
                .map { dto ->
                    Product(
                        id = dto.id,
                        title = dto.title,
                        price = dto.price,
                        description = dto.description,
                        category = dto.category,
                        image = dto.image,
                        brand = dto.brand ?: "Elara",
                        stock = 20,
                        sizes = listOf("S", "M", "L"),
                        color = dto.color ?: "Multi",
                        season = dto.season ?: "2026",
                        subcategory = "",
                        gender = com.example.proyect_final.util.GenderUtils.determineGender(
                            title = dto.title,
                            category = dto.category,
                            description = dto.description
                        )
                    )
                }
            // Inicializar caché de memoria con los productos locales
            localList.forEach { productsMemoryCache[it.id] = it }
            localList
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getProducts(limit: Long?): Result<List<Product>> {
        // Garantizar que la carga diferida (lazy) de productos locales ocurra
        val local = if (limit != null) cachedProducts.take(limit.toInt()) else cachedProducts
        
        return try {
            val response = apiService.getProducts()
            val apiProducts = response.documents.map { it.toDomain() }
            
            // Guardar los productos de la API en el caché en memoria
            apiProducts.forEach { productsMemoryCache[it.id] = it }
            
            // Combinar productos locales y de la API
            val combined = (apiProducts + local).distinctBy { it.id }
            val finalResult = if (limit != null) combined.take(limit.toInt()) else combined
            
            if (finalResult.isNotEmpty()) {
                Result.success(finalResult)
            } else {
                Result.success(local)
            }
        } catch (e: Exception) {
            // Si la API falla, devolvemos los productos de memoria
            val fallbackList = productsMemoryCache.values.toList()
            val finalFallback = if (limit != null) fallbackList.take(limit.toInt()) else fallbackList
            
            if (finalFallback.isNotEmpty()) {
                Result.success(finalFallback)
            } else {
                Result.success(local)
            }
        }
    }

    override suspend fun getProductById(id: String): Result<Product> {
        val local = cachedProducts
        
        // 1. Intentar obtener el producto directamente del caché de memoria
        val cached = productsMemoryCache[id]
        if (cached != null) {
            return Result.success(cached)
        }
        
        // 2. Si no está en memoria, intentar la petición de red poblando el caché
        return try {
            val productsResult = getProducts()
            if (productsResult.isSuccess) {
                val found = productsMemoryCache[id]
                if (found != null) {
                    return Result.success(found)
                }
            }
            
            // 3. Si falla y existe una prenda local con ese ID, devolverla
            val fallback = local.find { it.id == id }
            if (fallback != null) {
                Result.success(fallback)
            } else {
                // 4. Si todo lo anterior falla, devolver el primer producto disponible para evitar pantallas de error
                val firstAvailable = productsMemoryCache.values.firstOrNull() ?: local.firstOrNull()
                if (firstAvailable != null) {
                    Result.success(firstAvailable)
                } else {
                    Result.failure(Exception("Product not found"))
                }
            }
        } catch (e: Exception) {
            val fallback = local.find { it.id == id }
            if (fallback != null) {
                Result.success(fallback)
            } else {
                val firstAvailable = productsMemoryCache.values.firstOrNull() ?: local.firstOrNull()
                if (firstAvailable != null) {
                    Result.success(firstAvailable)
                } else {
                    Result.failure(e)
                }
            }
        }
    }
}
