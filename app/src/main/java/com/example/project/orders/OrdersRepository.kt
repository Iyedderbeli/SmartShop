package com.example.project.orders

import com.example.project.data.local.OrderDao
import com.example.project.data.local.OrderEntity
import com.example.project.data.local.OrderItemEntity
import kotlinx.coroutines.flow.Flow

class OrdersRepository(private val dao: OrderDao) {
    val orders: Flow<List<OrderEntity>> = dao.observeOrders()
    fun items(orderId: Int): Flow<List<OrderItemEntity>> = dao.observeOrderItems(orderId)
}
