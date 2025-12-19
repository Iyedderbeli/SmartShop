package com.example.project.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun OrderDetailsScreen(
    orderId: Int,
    onBack: () -> Unit,
    vm: OrdersViewModel = viewModel()
) {
    val itemsFlow = remember(orderId) { vm.orderItemsFlow(orderId) }
    val items by itemsFlow.collectAsState(initial = emptyList())
    val total = remember(items) { items.sumOf { it.price * it.quantity } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Order #$orderId", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Items: ${items.sumOf { it.quantity }}")
                Text("Total: ${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(
            visible = items.isNotEmpty(),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(180))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items, key = { "${it.orderId}-${it.productId}" }) { it ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp)) {
                            AsyncImage(
                                model = it.imageUri,
                                contentDescription = "Item image",
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(it.name, style = MaterialTheme.typography.titleMedium)
                                Text("Qty: ${it.quantity}")
                                Text("Unit: ${it.price}")
                            }
                            Text(
                                text = "${"%.2f".format(it.price * it.quantity)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
