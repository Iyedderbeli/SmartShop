package com.example.project.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun DashboardScreen(
    onGoProducts: () -> Unit,
    onGoCart: () -> Unit,
    onGoOrders: () -> Unit,
    onOpenOrder: (Int) -> Unit,
    vm: DashboardViewModel = viewModel()
) {
    val state by vm.ui.collectAsState()
    val df = remember { SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Dashboard", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Products",
                    value = "${state.productsCount}",
                    subtitle = "Stock: ${"%.2f".format(state.stockValue)}",
                    onClick = onGoProducts,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Cart",
                    value = "${state.cartItemsCount} items",
                    subtitle = "Value: ${"%.2f".format(state.cartValue)}",
                    onClick = onGoCart,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            StatCard(
                title = "Orders",
                value = "${state.ordersCount}",
                subtitle = "Revenue: ${"%.2f".format(state.revenueTotal)}",
                onClick = onGoOrders,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            ChartCard(
                title = "Revenue (last 7 days)",
                subtitle = "Daily totals",
                points = state.revenueLast7Days
            )
        }

        item {
            ChartCard(
                title = "Top products",
                subtitle = "Stock value (price × qty)",
                points = state.topProductsChart
            )
        }

        item {
            Text("Recent orders", style = MaterialTheme.typography.titleMedium)
        }

        if (state.recentOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("No orders yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("Checkout from Cart to create your first order.")
                    }
                }
            }
        } else {
            items(state.recentOrders, key = { it.orderId }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    onClick = { onOpenOrder(order.orderId) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Order #${order.orderId}", style = MaterialTheme.typography.titleMedium)
                        Text(df.format(Date(order.createdAtMillis)))
                        Spacer(Modifier.height(6.dp))
                        Text("Total: ${"%.2f".format(order.totalAmount)}")
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    points: List<BarPoint>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            if (points.isEmpty()) {
                Text("No data yet.")
            } else {
                BarChartWithLabels(points)
            }
        }
    }
}

@Composable
private fun BarChartWithLabels(points: List<BarPoint>) {
    val maxValue = remember(points) { max(1.0, points.maxOf { it.value }) }

    Column {
        // ✅ shorter height so labels fit comfortably
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val count = points.size
            val spacing = size.width * 0.04f
            val totalSpacing = spacing * (count + 1)
            val barWidth = (size.width - totalSpacing) / count
            val bottom = size.height

            points.forEachIndexed { index, point ->
                val x = spacing + index * (barWidth + spacing)
                val heightRatio = (point.value / maxValue).toFloat()
                val barHeight = heightRatio * (size.height * 0.90f)
                val top = bottom - barHeight

                drawRoundRect(
                    color = androidx.compose.ui.graphics.Color(0xFF4F8DFF),
                    topLeft = Offset(x, top),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(18f, 18f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ✅ names are always visible because screen is scrollable now
        Row(Modifier.fillMaxWidth()) {
            points.forEach { p ->
                Text(
                    text = p.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
