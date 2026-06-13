package com.example.proyect_final.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val photoUrl: String
)

@Serializable
data class PaymentMethod(
    val id: String,
    val cardNumber: String,
    val cardholderName: String,
    val expiryDate: String,
    val cvv: String
)

@Serializable
data class ShippingAddress(
    val id: String,
    val title: String,
    val fullAddress: String,
    val city: String,
    val postalCode: String
)

@Serializable
data class UserPreferences(
    val colorPalette: String = "Monocromo",
    val fitStyle: String = "Regular",
    val aiCuratorStyle: String = "Casual",
    val stockAlerts: Boolean = true,
    val weeklySummary: Boolean = true,
    val shareDataWithAi: Boolean = true,
    val cacheLocalAdvice: Boolean = true
)
