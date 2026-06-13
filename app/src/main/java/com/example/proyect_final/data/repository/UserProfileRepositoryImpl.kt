package com.example.proyect_final.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.proyect_final.domain.model.UserProfile
import com.example.proyect_final.domain.model.PaymentMethod
import com.example.proyect_final.domain.model.ShippingAddress
import com.example.proyect_final.domain.model.UserPreferences
import com.example.proyect_final.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserProfileRepositoryImpl(
    context: Context
) : UserProfileRepository {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "user_profile_prefs",
        Context.MODE_PRIVATE
    )

    private val json = Json { ignoreUnknownKeys = true }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    private val _shippingAddresses = MutableStateFlow<List<ShippingAddress>>(emptyList())
    private val _language = MutableStateFlow("Español")
    private val _userPreferences = MutableStateFlow(UserPreferences())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Load Profile
        val profileJson = sharedPrefs.getString("user_profile", null)
        _userProfile.value = if (profileJson != null) {
            try {
                json.decodeFromString<UserProfile>(profileJson)
            } catch (e: Exception) {
                null
            }
        } else {
            // Default user info if not set
            UserProfile(
                name = "Sofia Valery",
                email = "sofia.valery@example.com",
                phone = "+34 612 345 678",
                photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1000"
            )
        }

        // Load Payment Methods
        val paymentsJson = sharedPrefs.getString("payment_methods", null)
        _paymentMethods.value = if (paymentsJson != null) {
            try {
                json.decodeFromString<List<PaymentMethod>>(paymentsJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            // Default payment method
            listOf(
                PaymentMethod("1", "4242 4242 4242 4242", "Sofia Valery", "12/28", "123")
            )
        }

        // Load Shipping Addresses
        val addressesJson = sharedPrefs.getString("shipping_addresses", null)
        _shippingAddresses.value = if (addressesJson != null) {
            try {
                json.decodeFromString<List<ShippingAddress>>(addressesJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            // Default shipping address
            listOf(
                ShippingAddress("1", "Casa Principal", "Calle de Serrano, 45", "Madrid", "28001")
            )
        }

        // Load Language
        _language.value = sharedPrefs.getString("language", "Español") ?: "Español"

        // Load User Preferences
        val prefsJson = sharedPrefs.getString("user_preferences", null)
        _userPreferences.value = if (prefsJson != null) {
            try {
                json.decodeFromString<UserPreferences>(prefsJson)
            } catch (e: Exception) {
                UserPreferences()
            }
        } else {
            UserPreferences()
        }
    }

    override fun getUserProfile(): Flow<UserProfile?> = _userProfile.asStateFlow()

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val profileJson = json.encodeToString(profile)
            sharedPrefs.edit().putString("user_profile", profileJson).apply()
            _userProfile.value = profile
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPaymentMethods(): Flow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    override suspend fun savePaymentMethod(card: PaymentMethod): Result<Unit> {
        return try {
            val currentList = _paymentMethods.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == card.id }
            if (index >= 0) {
                currentList[index] = card
            } else {
                currentList.add(card)
            }
            val paymentsJson = json.encodeToString(currentList)
            sharedPrefs.edit().putString("payment_methods", paymentsJson).apply()
            _paymentMethods.value = currentList
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePaymentMethod(cardId: String): Result<Unit> {
        return try {
            val currentList = _paymentMethods.value.filter { it.id != cardId }
            val paymentsJson = json.encodeToString(currentList)
            sharedPrefs.edit().putString("payment_methods", paymentsJson).apply()
            _paymentMethods.value = currentList
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getShippingAddresses(): Flow<List<ShippingAddress>> = _shippingAddresses.asStateFlow()

    override suspend fun saveShippingAddress(address: ShippingAddress): Result<Unit> {
        return try {
            val currentList = _shippingAddresses.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == address.id }
            if (index >= 0) {
                currentList[index] = address
            } else {
                currentList.add(address)
            }
            val addressesJson = json.encodeToString(currentList)
            sharedPrefs.edit().putString("shipping_addresses", addressesJson).apply()
            _shippingAddresses.value = currentList
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteShippingAddress(addressId: String): Result<Unit> {
        return try {
            val currentList = _shippingAddresses.value.filter { it.id != addressId }
            val addressesJson = json.encodeToString(currentList)
            sharedPrefs.edit().putString("shipping_addresses", addressesJson).apply()
            _shippingAddresses.value = currentList
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveAllShippingAddresses(addresses: List<ShippingAddress>): Result<Unit> {
        return try {
            val addressesJson = json.encodeToString(addresses)
            sharedPrefs.edit().putString("shipping_addresses", addressesJson).apply()
            _shippingAddresses.value = addresses
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLanguage(): Flow<String> = _language.asStateFlow()

    override suspend fun saveLanguage(language: String): Result<Unit> {
        return try {
            sharedPrefs.edit().putString("language", language).apply()
            _language.value = language
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserPreferences(): Flow<UserPreferences> = _userPreferences.asStateFlow()

    override suspend fun saveUserPreferences(prefs: UserPreferences): Result<Unit> {
        return try {
            val prefsJson = json.encodeToString(prefs)
            sharedPrefs.edit().putString("user_preferences", prefsJson).apply()
            _userPreferences.value = prefs
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
