package com.example.project.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckoutDone: () -> Unit,
    vm: CartViewModel = viewModel()
) {
    val state by vm.ui.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cart",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.items) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text("Price: ${item.price}")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { vm.dec(item) }) { Text("-") }
                            Text("${item.quantityInCart}", modifier = Modifier.padding(horizontal = 8.dp))
                            TextButton(onClick = { vm.inc(item) }) { Text("+") }
                        }

                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { vm.remove(item) }) { Text("Remove") }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Total: ${"%.2f".format(state.total)}")

        state.error?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.checkout(onCheckoutDone) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.items.isNotEmpty()
        ) {
            Text("Checkout / Place Order")
        }
    }
}
