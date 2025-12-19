package com.example.project.dashboard

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.local.AppDatabase
import com.example.project.data.local.OrderEntity
import com.example.project.data.local.ProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

data class BarPoint(
    val label: String,
    val value: Double
)

data class DashboardUiState(
    val productsCount: Int = 0,
    val stockValue: Double = 0.0,

    val cartItemsCount: Int = 0,
    val cartValue: Double = 0.0,

    val ordersCount: Int = 0,
    val revenueTotal: Double = 0.0,

    val recentOrders: List<OrderEntity> = emptyList(),
    val topProducts: List<ProductEntity> = emptyList(),

    val revenueLast7Days: List<BarPoint> = emptyList(),
    val topProductsChart: List<BarPoint> = emptyList()
)

@RequiresApi(Build.VERSION_CODES.O)
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val _ui = MutableStateFlow(DashboardUiState())
    val ui: StateFlow<DashboardUiState> = _ui

    init {
        // Products stats + top products chart
        viewModelScope.launch {
            db.productDao.getProducts().collect { products ->
                val stockValue = products.sumOf { it.price * it.quantity }
                val top = products.sortedByDescending { it.price * it.quantity }.take(5)

                val topChart = top.map { p ->
                    // ✅ FULL product name as label
                    BarPoint(label = p.name, value = p.price * p.quantity)
                }

                _ui.update {
                    it.copy(
                        productsCount = products.size,
                        stockValue = stockValue,
                        topProducts = top,
                        topProductsChart = topChart
                    )
                }
            }
        }

        // Cart stats
        viewModelScope.launch {
            db.cartDao.observeCart().collect { cart ->
                val itemsCount = cart.sumOf { it.quantityInCart }
                val cartValue = cart.sumOf { it.price * it.quantityInCart }
                _ui.update { it.copy(cartItemsCount = itemsCount, cartValue = cartValue) }
            }
        }

        // Orders stats + revenue chart
        viewModelScope.launch {
            db.orderDao.observeOrders().collect { orders ->
                val totalRevenue = orders.sumOf { it.totalAmount }
                val recent = orders.take(5)

                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val days = (6 downTo 0).map { today.minusDays(it.toLong()) }

                val revenueByDay: Map<LocalDate, Double> =
                    orders.groupBy { o ->
                        Instant.ofEpochMilli(o.createdAtMillis).atZone(zone).toLocalDate()
                    }.mapValues { (_, list) ->
                        list.sumOf { it.totalAmount }
                    }

                val revenueChart = days.map { d ->
                    BarPoint(label = d.dayOfMonth.toString(), value = revenueByDay[d] ?: 0.0)
                }

                _ui.update {
                    it.copy(
                        ordersCount = orders.size,
                        revenueTotal = totalRevenue,
                        recentOrders = recent,
                        revenueLast7Days = revenueChart
                    )
                }
            }
        }
    }
}
