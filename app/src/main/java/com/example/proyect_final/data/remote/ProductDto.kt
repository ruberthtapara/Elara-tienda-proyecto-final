package com.example.proyect_final.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeLiSearchResponse(
    val results: List<MeLiProductDto>? = emptyList()
)

@Serializable
data class MeLiProductDto(
    val id: String? = null,
    val title: String? = null,
    val price: Double? = null,
    val thumbnail: String? = null,
    @SerialName("attributes") val attributes: List<MeLiAttribute>? = emptyList()
)

@Serializable
data class MeLiAttribute(
    val id: String? = null,
    val name: String? = null,
    @SerialName("value_name") val valueName: String? = null
)

@Serializable
data class ProductDto(
    val id: String,
    val title: String,
    val price: Double,
    val description: String = "",
    val category: String = "Moda",
    val image: String,
    val brand: String? = "Mercado Libre",
    val color: String? = "Varios",
    val season: String? = "Actual"
)

@Serializable
data class FirestoreResponse(
    val documents: List<FirestoreDocument> = emptyList()
)

@Serializable
data class FirestoreDocument(
    val name: String,
    val fields: FirestoreFields
)

@Serializable
data class FirestoreFields(
    val id: FirestoreValue? = null,
    val nombre: FirestoreValue? = null,
    val precio: FirestoreValue? = null,
    val descripcion: FirestoreValue? = null,
    val tipo: FirestoreValue? = null,
    val imagen: FirestoreValue? = null,
    val marca: FirestoreValue? = null,
    val stock: FirestoreValue? = null,
    val tallas: FirestoreValue? = null,
    val genero: FirestoreValue? = null,
    val subcategoria: FirestoreValue? = null
)

@Serializable
data class FirestoreValue(
    val stringValue: String? = null,
    val doubleValue: Double? = null,
    val integerValue: String? = null,
    val arrayValue: FirestoreArrayValue? = null
)

@Serializable
data class FirestoreArrayValue(
    val values: List<FirestoreValue> = emptyList()
)

fun FirestoreValue.toDoubleValue(): Double {
    return doubleValue ?: integerValue?.toDoubleOrNull() ?: stringValue?.toDoubleOrNull() ?: 0.0
}

fun FirestoreValue.toIntValue(): Int {
    return integerValue?.toIntOrNull() ?: doubleValue?.toInt() ?: stringValue?.toIntOrNull() ?: 0
}
