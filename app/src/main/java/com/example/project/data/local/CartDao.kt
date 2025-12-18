package com.example.project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun observeCart(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :id LIMIT 1")
    suspend fun getCartItem(id: Int): CartItemEntity?

    // ✅ NEW: fetch current cart as a one-time snapshot (for checkout)
    @Query("SELECT * FROM cart_items")
    suspend fun getCartOnce(): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :id")
    suspend fun remove(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}
