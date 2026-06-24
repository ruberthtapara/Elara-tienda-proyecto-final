package com.example.proyect_final.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_final.StyleGenApplication
import com.example.proyect_final.domain.model.UserProfile
import com.example.proyect_final.domain.model.PaymentMethod
import com.example.proyect_final.domain.model.ShippingAddress
import com.example.proyect_final.domain.model.UserPreferences
import com.example.proyect_final.domain.repository.UserProfileRepository
import com.example.proyect_final.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel(
    private val profileRepository: UserProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _userPurchases = MutableStateFlow<List<OrderHistoryItem>>(emptyList())
    val userPurchases: StateFlow<List<OrderHistoryItem>> = _userPurchases.asStateFlow()

    private var profileListener: ListenerRegistration? = null
    private var purchasesListener: ListenerRegistration? = null

    init {
        observeCurrentUserProfile()
    }

    private fun observeCurrentUserProfile() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                profileListener?.remove()
                purchasesListener?.remove()
                if (user != null) {
                    val uid = user.uid
                    profileListener = firestore.collection("users").document(uid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null || snapshot == null) {
                                viewModelScope.launch {
                                    profileRepository.getUserProfile().collect { localProfile ->
                                        if (_userProfile.value == null) {
                                            _userProfile.value = localProfile
                                        }
                                    }
                                }
                                return@addSnapshotListener
                            }
                            
                            val firstName = snapshot.getString("firstName") ?: ""
                            val lastName = snapshot.getString("lastName") ?: ""
                            val email = snapshot.getString("email") ?: user.email ?: ""
                            val phone = snapshot.getString("phone") ?: ""
                            val photoUrl = snapshot.getString("photoUrl") ?: ""
                            val name = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                                "$firstName $lastName".trim()
                            } else {
                                snapshot.getString("name") ?: ""
                            }
                            
                            val profile = UserProfile(
                                name = name,
                                email = email,
                                phone = phone,
                                photoUrl = photoUrl
                            )
                            _userProfile.value = profile
                            
                            // Load shipping addresses from Firestore
                            @Suppress("UNCHECKED_CAST")
                            val dbAddresses = snapshot.get("shipping_addresses") as? List<Map<String, Any>>
                            if (dbAddresses != null) {
                                val list = dbAddresses.mapNotNull { map ->
                                    val id = map["id"] as? String
                                    val title = map["title"] as? String
                                    val fullAddress = map["fullAddress"] as? String
                                    val city = map["city"] as? String
                                    val postalCode = map["postalCode"] as? String
                                    if (id != null && title != null && fullAddress != null && city != null && postalCode != null) {
                                        ShippingAddress(id, title, fullAddress, city, postalCode)
                                    } else {
                                        null
                                    }
                                }
                                viewModelScope.launch {
                                    profileRepository.saveAllShippingAddresses(list)
                                }
                            }
                            
                            viewModelScope.launch {
                                profileRepository.saveUserProfile(profile)
                            }
                        }

                    purchasesListener = firestore.collection("users").document(uid)
                        .collection("purchases")
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .addSnapshotListener { pSnapshot, pError ->
                            if (pError != null) {
                                android.util.Log.e("ProfileViewModel", "Error loading purchases", pError)
                                return@addSnapshotListener
                            }
                            if (pSnapshot != null) {
                                val list = pSnapshot.documents.mapNotNull { doc ->
                                    try {
                                        val total = doc.getDouble("totalAmount") ?: 0.0
                                        val time = doc.getLong("timestamp") ?: 0
                                        val itemsList = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                                        val parsedItems = itemsList.map { map ->
                                            val productMap = map["product"] as? Map<String, Any>
                                            OrderItem(
                                                title = productMap?.get("title") as? String ?: (map["title"] as? String ?: ""),
                                                quantity = (map["quantity"] as? Long)?.toInt() ?: 1,
                                                price = (map["price"] as? Double) ?: (productMap?.get("price") as? Double ?: 0.0),
                                                image = productMap?.get("image") as? String ?: (map["image"] as? String ?: "")
                                            )
                                        }
                                        val shipping = doc.get("shippingDetails") as? Map<String, Any>
                                        val address = shipping?.get("address") as? String ?: ""
                                        OrderHistoryItem(doc.id, total, time, parsedItems, address)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                _userPurchases.value = list
                            }
                        }
                } else {
                    _userProfile.value = null
                    _userPurchases.value = emptyList()
                }
            }
        }
    }

    val paymentMethods: StateFlow<List<PaymentMethod>> = profileRepository.getPaymentMethods()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val shippingAddresses: StateFlow<List<ShippingAddress>> = profileRepository.getShippingAddresses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedLanguage: StateFlow<String> = profileRepository.getLanguage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Español"
        )

    val userPreferences: StateFlow<UserPreferences> = profileRepository.getUserPreferences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun saveUserProfile(firstName: String, lastName: String, phone: String, photoUrl: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            val uid = user?.uid
            if (uid != null) {
                val email = user.email ?: ""
                val name = "$firstName $lastName".trim()
                
                val userMap = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "name" to name,
                    "phone" to phone,
                    "photoUrl" to photoUrl,
                    "email" to email
                )
                
                firestore.collection("users").document(uid)
                    .set(userMap, com.google.firebase.firestore.SetOptions.merge())
                
                val updated = UserProfile(name, email, phone, photoUrl)
                profileRepository.saveUserProfile(updated)
            }
        }
    }

    fun savePaymentMethod(id: String?, cardNumber: String, cardholderName: String, expiryDate: String, cvv: String) {
        viewModelScope.launch {
            val cardId = id ?: UUID.randomUUID().toString()
            val card = PaymentMethod(cardId, cardNumber, cardholderName, expiryDate, cvv)
            profileRepository.savePaymentMethod(card)
        }
    }

    fun deletePaymentMethod(cardId: String) {
        viewModelScope.launch {
            profileRepository.deletePaymentMethod(cardId)
        }
    }

    fun saveShippingAddress(id: String?, title: String, fullAddress: String, city: String, postalCode: String) {
        viewModelScope.launch {
            val addressId = id ?: UUID.randomUUID().toString()
            val address = ShippingAddress(addressId, title, fullAddress, city, postalCode)
            profileRepository.saveShippingAddress(address)
            
            // Sync to Firestore
            val user = authRepository.currentUser.first()
            val uid = user?.uid
            if (uid != null) {
                val allAddresses = profileRepository.getShippingAddresses().first()
                val addressesMap = allAddresses.map {
                    mapOf(
                        "id" to it.id,
                        "title" to it.title,
                        "fullAddress" to it.fullAddress,
                        "city" to it.city,
                        "postalCode" to it.postalCode
                    )
                }
                firestore.collection("users").document(uid)
                    .set(mapOf("shipping_addresses" to addressesMap), com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }

    fun deleteShippingAddress(addressId: String) {
        viewModelScope.launch {
            profileRepository.deleteShippingAddress(addressId)
            
            // Sync to Firestore
            val user = authRepository.currentUser.first()
            val uid = user?.uid
            if (uid != null) {
                val allAddresses = profileRepository.getShippingAddresses().first()
                val addressesMap = allAddresses.map {
                    mapOf(
                        "id" to it.id,
                        "title" to it.title,
                        "fullAddress" to it.fullAddress,
                        "city" to it.city,
                        "postalCode" to it.postalCode
                    )
                }
                firestore.collection("users").document(uid)
                    .set(mapOf("shipping_addresses" to addressesMap), com.google.firebase.firestore.SetOptions.merge())
            }
        }
    }

    fun saveLanguage(language: String) {
        viewModelScope.launch {
            profileRepository.saveLanguage(language)
        }
    }

    fun updateColorPalette(palette: String) {
        viewModelScope.launch {
            val current = userPreferences.value
            profileRepository.saveUserPreferences(current.copy(colorPalette = palette))
        }
    }

    fun updateFitStyle(fit: String) {
        viewModelScope.launch {
            val current = userPreferences.value
            profileRepository.saveUserPreferences(current.copy(fitStyle = fit))
        }
    }

    fun updateAiCuratorStyle(style: String) {
        viewModelScope.launch {
            val current = userPreferences.value
            profileRepository.saveUserPreferences(current.copy(aiCuratorStyle = style))
        }
    }

    fun updateStockAlerts(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferences.value
            profileRepository.saveUserPreferences(current.copy(stockAlerts = enabled))
        }
    }

    fun updateWeeklySummary(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferences.value
            profileRepository.saveUserPreferences(current.copy(weeklySummary = enabled))
        }
    }

    fun updateShareDataWithAi(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferences.value
            profileRepository.saveUserPreferences(current.copy(shareDataWithAi = enabled))
        }
    }

    fun updateCacheLocalAdvice(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferences.value
            profileRepository.saveUserPreferences(current.copy(cacheLocalAdvice = enabled))
        }
    }

    fun wipeLocalProfileData() {
        viewModelScope.launch {
            profileRepository.saveUserProfile(UserProfile("", "", "", ""))
            profileRepository.saveUserPreferences(UserPreferences())
            // Remove payment cards and addresses
            val currentCards = paymentMethods.value
            currentCards.forEach { profileRepository.deletePaymentMethod(it.id) }
            val currentAddresses = shippingAddresses.value
            currentAddresses.forEach { profileRepository.deleteShippingAddress(it.id) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        profileListener?.remove()
        purchasesListener?.remove()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StyleGenApplication)
                ProfileViewModel(
                    profileRepository = application.container.userProfileRepository,
                    authRepository = application.container.authRepository
                )
            }
        }
    }
}

data class OrderItem(
    val title: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val image: String = ""
)

data class OrderHistoryItem(
    val orderId: String = "",
    val totalAmount: Double = 0.0,
    val timestamp: Long = 0,
    val items: List<OrderItem> = emptyList(),
    val address: String = ""
)
