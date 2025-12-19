package com.example.project.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.project.data.local.ProductEntity
import com.example.project.orders.CartViewModel
import kotlinx.coroutines.launch

@Composable
fun ProductScreen(
    vm: ProductViewModel = viewModel()
) {
    val state by vm.ui.collectAsState()
    val cartVm: CartViewModel = viewModel()

    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var deleteTarget by remember { mutableStateOf<ProductEntity?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        vm.onImageSelected(uri?.toString())
    }

    // 🔴 Delete confirmation popup
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete product?") },
            text = { Text("This will remove the product permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        val p = deleteTarget!!
                        deleteTarget = null
                        vm.delete(p)
                        scope.launch {
                            snackbar.showSnackbar("Deleted: ${p.name}")
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .animateContentSize()
        ) {

            // 📊 Stats card (clean, no Live button)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Overview", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Total products: ${state.totalProducts}")
                    Text("Stock value: ${"%.2f".format(state.totalStockValue)}")
                }
            }

            Spacer(Modifier.height(14.dp))

            // ➕ Add / Edit product form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        text = if (state.editingId != null) "Edit product" else "Add new product",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = vm::onNameChange,
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    Row {
                        OutlinedTextField(
                            value = state.quantity,
                            onValueChange = vm::onQtyChange,
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(Modifier.width(10.dp))
                        OutlinedTextField(
                            value = state.price,
                            onValueChange = vm::onPriceChange,
                            label = { Text("Price") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { pickImageLauncher.launch("image/*") },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Choose image")
                        }

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = if (state.imageUri != null) "Image selected ✅" else "No image",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    AnimatedVisibility(
                        visible = state.imageUri != null,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(180))
                    ) {
                        Spacer(Modifier.height(10.dp))
                        AsyncImage(
                            model = state.imageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn(tween(180)),
                        exit = fadeOut(tween(180))
                    ) {
                        Spacer(Modifier.height(10.dp))
                        state.errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (state.editingId != null) {
                            TextButton(onClick = vm::cancelEdit) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = vm::save) {
                                Text("Save")
                            }
                        } else {
                            Button(onClick = vm::save) {
                                Text("Add product")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 📦 Products list
            Text("Products", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(state.products, key = { _, p -> p.id }) { _, product ->
                    ProductCard(
                        product = product,
                        onAddToCart = {
                            cartVm.addToCart(product)
                            scope.launch {
                                snackbar.showSnackbar("Added to cart: ${product.name}")
                            }
                        },
                        onEdit = { vm.startEdit(product) },
                        onDelete = { deleteTarget = product }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductEntity,
    onAddToCart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUri,
                contentDescription = "Product image",
                modifier = Modifier.size(64.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text("Qty: ${product.quantity} • Price: ${product.price}")
            }

            IconButton(onClick = onAddToCart) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = "Add to cart")
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}
