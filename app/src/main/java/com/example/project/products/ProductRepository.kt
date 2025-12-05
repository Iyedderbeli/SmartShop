package com.example.project.products

import com.example.project.data.local.ProductDao
import com.example.project.data.local.ProductEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val dao: ProductDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val productsCollection = firestore.collection("products")

    val products: Flow<List<ProductEntity>> = dao.getProducts()

    suspend fun addProduct(name: String, quantity: Int, price: Double) {
        require(name.isNotBlank()) { "Name cannot be empty" }
        require(price > 0) { "Price must be > 0" }
        require(quantity >= 0) { "Quantity must be ≥ 0" }

        val product = ProductEntity(
            name = name,
            quantity = quantity,
            price = price
        )

        // 1) Save locally (Room)
        dao.insertProduct(product)

        // 2) Save in Firestore (cloud)
        val data = hashMapOf(
            "name" to name,
            "quantity" to quantity,
            "price" to price
        )

        productsCollection.add(data)
            .addOnSuccessListener {
                // optional: log or show success
            }
            .addOnFailureListener { e ->
                // optional: handle error (Toast, log, etc.)
            }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        // For now: delete local only
        dao.deleteProduct(product)
        // Bonus (optional): you could also delete from Firestore if you tracked doc IDs.
    }
}
