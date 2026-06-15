package com.example.proyect_final.data.repository

import com.example.proyect_final.domain.model.Product
import com.example.proyect_final.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseProductRepositoryImpl(
    private val db: FirebaseFirestore
) : ProductRepository {

    override suspend fun getProducts(limit: Long?): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val query = if (limit != null) {
                db.collection("productos").limit(limit)
            } else {
                db.collection("productos")
            }
            
            val snapshot = query.get().await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toDomain()
            }
            Result.success(products)
        } catch (e: Exception) {
            android.util.Log.e("Firestore", "Error getting products", e)
            Result.failure(e)
        }
    }

    override suspend fun getProductById(id: String): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val doc = db.collection("productos").document(id).get().await()
            val product = doc.toDomain()
            if (product != null) {
                Result.success(product)
            } else {
                Result.failure(Exception("Producto no encontrado"))
            }
        } catch (e: Exception) {
            android.util.Log.e("Firestore", "Error getting product by id", e)
            Result.failure(e)
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toDomain(): Product? {
        return try {
            val imgUrl = getString("imagen") ?: ""
            android.util.Log.d("Firestore", "Cargando producto: ${getString("nombre")}, URL: $imgUrl")
            Product(
                id = id,
                title = getString("nombre") ?: "Sin nombre",
                price = getDouble("precio") ?: 0.0,
                description = getString("descripcion") ?: "Sin descripción",
                category = getString("tipo") ?: "Ropa",
                image = imgUrl,
                brand = getString("marca") ?: "Genérica",
                stock = getLong("stock")?.toInt() ?: 0,
                sizes = (get("tallas") as? List<*>)?.map { it.toString() } ?: emptyList(),
                subcategory = getString("subcategoria") ?: "",
                gender = com.example.proyect_final.util.GenderUtils.determineGender(
                    title = getString("nombre") ?: "Sin nombre",
                    category = getString("tipo") ?: "Ropa",
                    description = getString("descripcion") ?: "Sin descripción",
                    existingGender = getString("genero")
                )
            )
        } catch (e: Exception) {
            null
        }
    }
}
