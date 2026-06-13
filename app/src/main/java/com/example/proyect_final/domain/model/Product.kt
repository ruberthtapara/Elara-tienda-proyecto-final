package com.example.proyect_final.domain.model

data class Product(
    val id: String,
    val title: String,
    val price: Double,
    val description: String,
    val category: String, // 'tipo' en Firestore: Ropa / Accesorio
    val image: String,
    val brand: String, // 'marca' en Firestore
    val stock: Int = 0,
    val sizes: List<String> = emptyList(), // 'tallas' en Firestore
    val color: String = "Multi",
    val season: String = "2026",
    val gender: String = "Unisex",
    val subcategory: String = ""
)
