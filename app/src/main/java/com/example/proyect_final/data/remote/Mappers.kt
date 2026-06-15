package com.example.proyect_final.data.remote

import com.example.proyect_final.domain.model.Product

fun MeLiProductDto.toDomain(): Product {
    // Extract Brand from attributes if possible
    val brandAttr = attributes?.find { it.id == "BRAND" }?.valueName ?: "Elara Curated"
    val colorAttr = attributes?.find { it.id == "COLOR" }?.valueName ?: "Multicolor"
    
    // Convert insecure http thumbnail to https and get higher resolution if possible
    val secureImage = thumbnail?.replace("http://", "https://") 
        ?.replace("-I.jpg", "-O.jpg") // MeLi trick for better resolution
        ?: "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?q=80&w=1000"

    val resolvedTitle = title ?: "Pieza Exclusiva"
    val resolvedCategory = "Moda"
    val resolvedDesc = "Producto original disponible en el catálogo de Elara. Calidad garantizada y estilo contemporáneo."
    return Product(
        id = id ?: "0",
        title = resolvedTitle,
        price = price ?: 0.0,
        description = resolvedDesc,
        category = resolvedCategory,
        image = secureImage,
        brand = brandAttr,
        stock = 10,
        sizes = listOf("S", "M", "L"),
        color = colorAttr,
        season = "Colección 2026",
        gender = com.example.proyect_final.util.GenderUtils.determineGender(resolvedTitle, resolvedCategory, resolvedDesc),
        subcategory = ""
    )
}

fun ProductDto.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        price = price,
        description = description,
        category = category,
        image = image,
        brand = brand ?: "Elara Fashion",
        stock = 15,
        sizes = listOf("M", "L", "XL"),
        color = color ?: "Multi",
        season = season ?: "Colección Actual",
        gender = com.example.proyect_final.util.GenderUtils.determineGender(title, category, description, existingGender = null),
        subcategory = ""
    )
}

fun FirestoreDocument.toDomain(): Product {
    val docId = name.substringAfterLast("/")
    val numericId = fields.id?.toIntValue() ?: docId.hashCode().coerceAtLeast(0)

    return Product(
        id = numericId.toString(),
        title = fields.nombre?.stringValue ?: "Sin nombre",
        price = fields.precio?.toDoubleValue() ?: 0.0,
        description = fields.descripcion?.stringValue ?: "Sin descripción",
        category = fields.tipo?.stringValue ?: "Otros",
        image = fields.imagen?.stringValue ?: "",
        brand = fields.marca?.stringValue ?: "Elara",
        stock = fields.stock?.toIntValue() ?: 0,
        sizes = fields.tallas?.arrayValue?.values?.mapNotNull { it.stringValue } ?: emptyList(),
        color = "Varios",
        season = "Colección 2026",
        gender = com.example.proyect_final.util.GenderUtils.determineGender(
            title = fields.nombre?.stringValue ?: "Sin nombre",
            category = fields.tipo?.stringValue ?: "Otros",
            description = fields.descripcion?.stringValue ?: "Sin descripción",
            existingGender = fields.genero?.stringValue
        ),
        subcategory = fields.subcategoria?.stringValue ?: ""
    )
}

fun getProductImageModel(imageUrl: String): Any {
    return if (imageUrl.startsWith("data:image/")) {
        try {
            val base64Data = imageUrl.substringAfter(",")
            android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            imageUrl
        }
    } else {
        imageUrl
    }
}
