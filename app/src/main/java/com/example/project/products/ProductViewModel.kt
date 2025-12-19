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

    private val repo: ProductRepository
    private val _ui = MutableStateFlow(ProductUiState())
    val ui: StateFlow<ProductUiState> = _ui

    init {
        val db = AppDatabase.getInstance(application)
        repo = ProductRepository(db.productDao)

        viewModelScope.launch {
            repo.products.collect { list ->
                val totalProducts = list.size
                val totalStockValue = list.sumOf { it.quantity * it.price }
                _ui.update { it.copy(products = list, totalProducts = totalProducts, totalStockValue = totalStockValue) }
            }
        }
    }

    fun onNameChange(v: String) = _ui.update { it.copy(name = v) }
    fun onQtyChange(v: String) = _ui.update { it.copy(quantity = v) }
    fun onPriceChange(v: String) = _ui.update { it.copy(price = v) }
    fun onImageSelected(uri: String?) = _ui.update { it.copy(imageUri = uri) }

    fun startEdit(p: ProductEntity) {
        _ui.update {
            it.copy(
                name = p.name,
                quantity = p.quantity.toString(),
                price = p.price.toString(),
                imageUri = p.imageUri,
                editingId = p.id,
                errorMessage = null
            )
        }
    }

    fun cancelEdit() {
        _ui.update {
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

    fun save() {
        val s = _ui.value
        val q = s.quantity.toIntOrNull()
        val pr = s.price.toDoubleOrNull()

        if (q == null || pr == null) {
            _ui.update { it.copy(errorMessage = "Quantity and price must be numbers") }
            return
        }

        viewModelScope.launch {
            try {
                val id = s.editingId
                if (id == null) repo.addProduct(s.name.trim(), q, pr, s.imageUri)
                else repo.updateProduct(ProductEntity(id = id, name = s.name.trim(), quantity = q, price = pr, imageUri = s.imageUri))
                cancelEdit()
            } catch (e: Exception) {
                _ui.update { it.copy(errorMessage = e.message ?: "Error") }
            }
        }
    }

    fun delete(p: ProductEntity) {
        viewModelScope.launch { repo.deleteProduct(p) }
    }
}
