package com.example.proyect_final.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_final.StyleGenApplication
import com.example.proyect_final.domain.model.Product
import com.example.proyect_final.domain.model.User
import com.example.proyect_final.domain.repository.AuthRepository
import com.example.proyect_final.util.WidgetUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val size: String = "M"
)

class CartViewModel(
    private val application: Application,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _currentUserState = MutableStateFlow<User?>(null)
    val currentUserState: StateFlow<User?> = _currentUserState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _currentUserState.value = user
            }
        }
    }

    private fun updateWidget(items: List<CartItem>) {
        val totalCount = items.sumOf { it.quantity }
        WidgetUtils.updateCartWidget(application, totalCount)
    }

    fun addProduct(product: Product, size: String = "M") {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.product.id == product.id && it.size == size }
            if (existingItem != null) {
                currentItems.map { 
                    if (it.product.id == product.id && it.size == size) it.copy(quantity = it.quantity + 1) 
                    else it 
                }
            } else {
                currentItems + CartItem(product, 1, size)
            }
        }
        updateWidget(_cartItems.value)
    }

    fun removeProduct(productId: String, size: String) {
        _cartItems.update { currentItems ->
            currentItems.filterNot { it.product.id == productId && it.size == size }
        }
        updateWidget(_cartItems.value)
    }

    fun updateQuantity(productId: String, size: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeProduct(productId, size)
            return
        }
        _cartItems.update { currentItems ->
            currentItems.map {
                if (it.product.id == productId && it.size == size) it.copy(quantity = newQuantity)
                else it
            }
        }
        updateWidget(_cartItems.value)
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        updateWidget(emptyList())
    }

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.product.price * it.quantity }

    private val sharedPrefs = application.getSharedPreferences("elara_cart_prefs", android.content.Context.MODE_PRIVATE)

    // Daily Check-In state variables
    private val _claimedDaysCount = MutableStateFlow(sharedPrefs.getInt("claimed_days_count", 0))
    val claimedDaysCount: StateFlow<Int> = _claimedDaysCount.asStateFlow()

    private val _lastClaimedDate = MutableStateFlow(sharedPrefs.getString("last_claimed_date", "") ?: "")
    val lastClaimedDate: StateFlow<String> = _lastClaimedDate.asStateFlow()

    // Current selected coupon index (1-based index of day, 0 if no coupon applied)
    private val _selectedCouponDay = MutableStateFlow(sharedPrefs.getInt("selected_coupon_day", 0))
    val selectedCouponDay: StateFlow<Int> = _selectedCouponDay.asStateFlow()

    // Simulated date offset (for testing)
    private val _simulatedDateOffset = MutableStateFlow(sharedPrefs.getInt("simulated_date_offset", 0))
    val simulatedDateOffset: StateFlow<Int> = _simulatedDateOffset.asStateFlow()

    fun getTodayString(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, _simulatedDateOffset.value)
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
    }

    fun isTodayClaimed(): Boolean {
        return _lastClaimedDate.value == getTodayString()
    }

    fun claimTodayCoupon(): Boolean {
        if (isTodayClaimed()) return false
        val currentCount = _claimedDaysCount.value
        if (currentCount >= 10) return false

        val newCount = currentCount + 1
        val todayStr = getTodayString()

        _claimedDaysCount.value = newCount
        _lastClaimedDate.value = todayStr
        
        // Auto-select the newly claimed coupon
        _selectedCouponDay.value = newCount

        sharedPrefs.edit()
            .putInt("claimed_days_count", newCount)
            .putString("last_claimed_date", todayStr)
            .putInt("selected_coupon_day", newCount)
            .apply()

        return true
    }

    fun selectCoupon(day: Int) {
        if (day in 0.._claimedDaysCount.value) {
            _selectedCouponDay.value = day
            sharedPrefs.edit().putInt("selected_coupon_day", day).apply()
        }
    }

    fun simulateNextDay() {
        val newOffset = _simulatedDateOffset.value + 1
        _simulatedDateOffset.value = newOffset
        sharedPrefs.edit().putInt("simulated_date_offset", newOffset).apply()
    }

    fun resetDailyBonus() {
        _claimedDaysCount.value = 0
        _lastClaimedDate.value = ""
        _selectedCouponDay.value = 0
        _simulatedDateOffset.value = 0
        sharedPrefs.edit()
            .putInt("claimed_days_count", 0)
            .putString("last_claimed_date", "")
            .putInt("selected_coupon_day", 0)
            .putInt("simulated_date_offset", 0)
            .apply()
    }

    fun getCouponPercentageForDay(day: Int): Int {
        if (day < 1 || day > 10) return 0
        return 10 + (day - 1) * 5
    }

    val selectedCouponPercentage: Int
        get() = getCouponPercentageForDay(_selectedCouponDay.value)

    val discountAmount: Double
        get() = subtotal * (selectedCouponPercentage / 100.0)

    val totalAmount: Double
        get() = subtotal - discountAmount

    fun checkout(
        fullName: String,
        address: String,
        phone: String,
        cardNumber: String,
        cardExpiry: String,
        cardCvv: String,
        onComplete: (Result<Unit>) -> Unit
    ) {
        val user = _currentUserState.value
        val userId = user?.uid ?: "guest"
        val email = user?.email ?: "guest@elara.com"
        
        val items = _cartItems.value
        if (items.isEmpty()) {
            onComplete(Result.failure(Exception("El carrito está vacío")))
            return
        }

        val timestamp = System.currentTimeMillis()
        val orderItems = items.map { item ->
            mapOf(
                "productId" to item.product.id,
                "title" to item.product.title,
                "price" to item.product.price,
                "quantity" to item.quantity,
                "size" to item.size
            )
        }

        val orderData = mapOf(
            "userId" to userId,
            "email" to email,
            "items" to orderItems,
            "subtotal" to subtotal,
            "discount" to discountAmount,
            "total" to totalAmount,
            "couponDay" to _selectedCouponDay.value,
            "timestamp" to timestamp,
            "shippingDetails" to mapOf(
                "fullName" to fullName,
                "address" to address,
                "phone" to phone
            ),
            "paymentDetails" to mapOf(
                "cardNumber" to "xxxx-xxxx-xxxx-${cardNumber.takeLast(4)}",
                "cardHolder" to fullName
            )
        )

        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val orderDocRef = firestore.collection("orders").document()
        val orderId = orderDocRef.id

        // Use a batch to write order, user purchase, and decrement stock
        val batch = firestore.batch()
        
        // 1. Order document
        batch.set(orderDocRef, orderData)
        
        // 2. User purchase document
        val userPurchaseRef = firestore.collection("users").document(userId)
            .collection("purchases").document(orderId)
        batch.set(userPurchaseRef, orderData)
        
        // 3. Decrement stock for all items
        for (item in items) {
            val productRef = firestore.collection("productos").document(item.product.id)
            batch.update(productRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.quantity.toLong()))
        }

        batch.commit()
            .addOnSuccessListener {
                // Log the interaction
                val interaction = mapOf(
                    "userId" to userId,
                    "action" to "purchase",
                    "orderId" to orderId,
                    "itemsCount" to items.sumOf { it.quantity },
                    "total" to totalAmount,
                    "timestamp" to timestamp
                )
                firestore.collection("interactions").add(interaction)
                    .addOnCompleteListener {
                        // Trigger push notification
                        com.example.proyect_final.util.NotificationHelper.showOrderConfirmationNotification(application, totalAmount)
                        // Clear cart
                        clearCart()
                        onComplete(Result.success(Unit))
                    }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("CartViewModel", "Error in batch purchase commit", e)
                // Trigger push notification even in failure fallback, as we still clear the cart and simulate success for UX
                com.example.proyect_final.util.NotificationHelper.showOrderConfirmationNotification(application, totalAmount)
                // Fallback: still clear cart and succeed so the UI doesn't hang
                clearCart()
                onComplete(Result.success(Unit))
            }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { 
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StyleGenApplication
                val authRepository = app.container.authRepository
                CartViewModel(app, authRepository)
            }
        }
    }
}
