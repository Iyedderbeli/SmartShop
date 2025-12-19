package com.example.project.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.local.AppDatabase
import com.example.project.data.local.OrderEntity
import com.example.project.data.local.OrderItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val orders: List<OrderEntity> = emptyList()
)

data class OrderDetailsUiState(
    val items: List<OrderItemEntity> = emptyList(),
    val total: Double = 0.0
)

class OrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: OrdersRepository
    private val _ui = MutableStateFlow(OrdersUiState())
    val ui: StateFlow<OrdersUiState> = _ui

    init {
        val db = AppDatabase.getInstance(application)
        repo = OrdersRepository(db.orderDao)

        viewModelScope.launch {
            repo.orders.collect { list ->
                _ui.update { it.copy(orders = list) }
            }
        }
    }

    fun orderItemsFlow(orderId: Int) = repo.items(orderId)
}
