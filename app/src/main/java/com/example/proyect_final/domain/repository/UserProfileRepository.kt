package com.example.proyect_final.domain.repository

import com.example.proyect_final.domain.model.UserProfile
import com.example.proyect_final.domain.model.PaymentMethod
import com.example.proyect_final.domain.model.ShippingAddress
import com.example.proyect_final.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit>
    
    fun getPaymentMethods(): Flow<List<PaymentMethod>>
    suspend fun savePaymentMethod(card: PaymentMethod): Result<Unit>
    suspend fun deletePaymentMethod(cardId: String): Result<Unit>
    
    fun getShippingAddresses(): Flow<List<ShippingAddress>>
    suspend fun saveShippingAddress(address: ShippingAddress): Result<Unit>
    suspend fun deleteShippingAddress(addressId: String): Result<Unit>
    suspend fun saveAllShippingAddresses(addresses: List<ShippingAddress>): Result<Unit>
    
    fun getLanguage(): Flow<String>
    suspend fun saveLanguage(language: String): Result<Unit>

    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun saveUserPreferences(prefs: UserPreferences): Result<Unit>
}
