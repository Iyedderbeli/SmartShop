package com.example.project.data.local

import androidx.room.Entity

@Entity(
    tableName = "order_items",
    primaryKeys = ["orderId", "productId"]
)
data class OrderItemEntity(
    val orderId: Int,
    val productId: Int,
    val name: String,
    val price: Double,
    val imageUri: String? = null,
    val quantity: Int
)
