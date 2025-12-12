package com.example.project.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.project.data.local.ProductEntity

@Composable
fun ProductScreen(
    viewModel: ProductViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri?.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Add product", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        TextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Product name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = state.quantity,
            onValueChange = viewModel::onQuantityChange,
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = state.price,
            onValueChange = viewModel::onPriceChange,
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { pickImageLauncher.launch("image/*") }) {
                Text("Choose image")
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (state.imageUri != null) "Image selected ✅" else "No image",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        state.imageUri?.let { uriString ->
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = uriString,
                contentDescription = "Selected product image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = viewModel::addProduct,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add product")
        }

        state.errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(text = msg, color = Color.Red)
        }

        Spacer(Modifier.height(16.dp))

        // Stats
        Text("Statistics", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("Total products: ${state.totalProducts}")
        Text("Total stock value: ${"%.2f".format(state.totalStockValue)}")

        Spacer(Modifier.height(16.dp))

        // List
        Text("Products", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
            .clickable { onDelete() } // tap to delete (simple)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

            product.imageUri?.let { uriString ->
                AsyncImage(
                    model = uriString,
                    contentDescription = "Product image",
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp)
                )
                Spacer(Modifier.width(12.dp))
            }

            Column {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text("Qty: ${product.quantity}")
                Text("Price: ${product.price}")
            }
        }
    }
}
