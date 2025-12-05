package com.example.project.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.data.local.ProductEntity

@Composable
fun ProductScreen(
    viewModel: ProductViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Form
        TextField(
            value = state.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Product name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = state.quantity,
            onValueChange = { viewModel.onQuantityChange(it) },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = state.price,
            onValueChange = { viewModel.onPriceChange(it) },
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.addProduct() },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add product")
        }

        state.errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(text = msg, color = Color.Red)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text("Total products: ${state.totalProducts}")
        Text("Total stock value: ${"%.2f".format(state.totalStockValue)}")

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Products",
            style = MaterialTheme.typography.titleMedium
        )


        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.products) { product ->
                ProductItem(
                    product = product,
                    onDelete = { viewModel.deleteProduct(product) }
                )
            }
        }
    }
}

@Composable
fun ProductItem(
    product: ProductEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onDelete() } // tap to delete (simple for now)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Qty: ${product.quantity} units")
            Text(text = "Price: ${product.price}dt")
        }
    }
}
