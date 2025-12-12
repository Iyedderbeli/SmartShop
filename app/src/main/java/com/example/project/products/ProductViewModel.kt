package com.example.project.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.local.AppDatabase
import com.example.project.data.local.ProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<ProductEntity> = emptyList(),
    val name: String = "",
    val quantity: String = "",
    val price: String = "",
    val imageUri: String? = null,
    val editingId: Int? = null,
    val errorMessage: String? = null,
    val totalProducts: Int = 0,
    val totalStockValue: Double = 0.0
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState

    init {
        val db = AppDatabase.getInstance(application)
        repository = ProductRepository(db.productDao)

        viewModelScope.launch {
            repository.products.collect { list ->
                val totalProducts = list.size
                val totalStockValue = list.sumOf { it.quantity * it.price }

                _uiState.update {
                    it.copy(
                        products = list,
                        totalProducts = totalProducts,
                        totalStockValue = totalStockValue
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onQuantityChange(value: String) {
        _uiState.update { it.copy(quantity = value) }
    }

    fun onPriceChange(value: String) {
        _uiState.update { it.copy(price = value) }
    }

    fun onImageSelected(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun startEdit(product: ProductEntity) {
        _uiState.update {
            it.copy(
                name = product.name,
                quantity = product.quantity.toString(),
                price = product.price.toString(),
                imageUri = product.imageUri,
                editingId = product.id,
                errorMessage = null
            )
        }
    }

    fun cancelEdit() {
        _uiState.update {
            it.copy(
                name = "",
                quantity = "",
                price = "",
                imageUri = null,
                editingId = null,
                errorMessage = null
            )
        }
    }

    fun saveProduct() {
        val current = _uiState.value
        val name = current.name.trim()
        val quantityInt = current.quantity.toIntOrNull()
        val priceDouble = current.price.toDoubleOrNull()

        if (quantityInt == null || priceDouble == null) {
            _uiState.update { it.copy(errorMessage = "Quantity and price must be numbers") }
            return
        }

        viewModelScope.launch {
            try {
                val id = current.editingId

                if (id == null) {
                    // Add
                    repository.addProduct(name, quantityInt, priceDouble, current.imageUri)
                } else {
                    // Update
                    repository.updateProduct(
                        ProductEntity(
                            id = id,
                            name = name,
                            quantity = quantityInt,
                            price = priceDouble,
                            imageUri = current.imageUri
                        )
                    )
                }

                cancelEdit()
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}
