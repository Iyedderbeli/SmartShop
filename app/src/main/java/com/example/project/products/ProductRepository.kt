package com.example.project.products

import com.example.project.data.local.ProductDao
import com.example.project.data.local.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val dao: ProductDao
) {
    val products: Flow<List<ProductEntity>> = dao.getProducts()

    suspend fun addProduct(
        name: String,
        quantity: Int,
        price: Double,
        imageUri: String?
    ) {
        require(name.isNotBlank()) { "Name cannot be empty" }
        require(price > 0) { "Price must be > 0" }
        require(quantity >= 0) { "Quantity must be ≥ 0" }

        dao.insertProduct(
            ProductEntity(
                name = name,
                quantity = quantity,
                price = price,
                imageUri = imageUri
            )
        )
    }

    suspend fun deleteProduct(product: ProductEntity) {
        dao.deleteProduct(product)
    }
}
