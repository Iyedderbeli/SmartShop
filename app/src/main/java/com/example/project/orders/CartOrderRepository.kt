package com.example.project.orders

import com.example.project.data.local.*

import kotlinx.coroutines.flow.Flow

class CartOrderRepository(
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) {
    val cart: Flow<List<CartItemEntity>> = cartDao.observeCart()
    val orders: Flow<List<OrderEntity>> = orderDao.observeOrders()

    suspend fun addToCart(product: ProductEntity) {
        val existing = cartDao.getCartItem(product.id)
        val newQty = (existing?.quantityInCart ?: 0) + 1
        cartDao.upsert(
            CartItemEntity(
                productId = product.id,
                name = product.name,
                price = product.price,
                imageUri = product.imageUri,
                quantityInCart = newQty
            )
        )
    }

    suspend fun setCartQuantity(productId: Int, newQty: Int) {
        if (newQty <= 0) {
            cartDao.remove(productId)
            return
        }
        val existing = cartDao.getCartItem(productId) ?: return
        cartDao.upsert(existing.copy(quantityInCart = newQty))
    }

    suspend fun removeFromCart(productId: Int) {
        cartDao.remove(productId)
    }

    suspend fun checkout(): Int {
        val snapshot = cartDao.getCartOnce()
        if (snapshot.isEmpty()) throw IllegalArgumentException("Cart is empty")

        val total = snapshot.sumOf { it.price * it.quantityInCart }
        val orderId = orderDao.insertOrder(
            OrderEntity(createdAtMillis = System.currentTimeMillis(), totalAmount = total)
        ).toInt()

        val items = snapshot.map {
            OrderItemEntity(
                orderId = orderId,
                productId = it.productId,
                name = it.name,
                price = it.price,
                imageUri = it.imageUri,
                quantity = it.quantityInCart
            )
        }

        orderDao.insertOrderItems(items)
        cartDao.clear()
        return orderId
    }
}
