package com.example.project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Int,   // same id as ProductEntity
    val name: String,
    val price: Double,
    val imageUri: String? = null,
    val quantityInCart: Int
)
