package com.example.project.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.local.AppDatabase
import com.example.project.data.local.CartItemEntity
import com.example.project.data.local.ProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItemEntity> = emptyList(),
    val total: Double = 0.0,
    val error: String? = null
)

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: CartOrderRepository

    private val _ui = MutableStateFlow(CartUiState())
    val ui: StateFlow<CartUiState> = _ui

    init {
        val db = AppDatabase.getInstance(application)
        repo = CartOrderRepository(db.cartDao, db.orderDao)

        viewModelScope.launch {
            repo.cart.collect { list ->
                val total = list.sumOf { it.price * it.quantityInCart }
                _ui.update { it.copy(items = list, total = total) }
            }
        }
    }

    fun addToCart(product: ProductEntity) {
        viewModelScope.launch { repo.addToCart(product) }
    }

    fun inc(item: CartItemEntity) {
        viewModelScope.launch { repo.setCartQuantity(item.productId, item.quantityInCart + 1) }
    }

    fun dec(item: CartItemEntity) {
        viewModelScope.launch { repo.setCartQuantity(item.productId, item.quantityInCart - 1) }
    }

    fun remove(item: CartItemEntity) {
        viewModelScope.launch { repo.removeFromCart(item.productId) }
    }

    fun checkout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.checkout()
                _ui.update { it.copy(error = null) }
                onSuccess()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message ?: "Checkout failed") }
            }
        }
    }
}
