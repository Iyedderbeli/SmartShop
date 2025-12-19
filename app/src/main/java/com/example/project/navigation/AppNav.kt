package com.example.project.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project.auth.LoginScreen
import com.example.project.auth.LoginViewModel
import com.example.project.dashboard.DashboardScreen
import com.example.project.orders.CartScreen
import com.example.project.orders.OrderDetailsScreen
import com.example.project.orders.OrderHistoryScreen
import com.example.project.products.ProductScreen

private object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val ORDER_DETAILS = "order_details"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute == Routes.DASHBOARD ||
            currentRoute == Routes.PRODUCTS ||
            currentRoute == Routes.CART ||
            currentRoute == Routes.ORDERS

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = LoginViewModel(),
                    onLoginSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onGoProducts = { navController.navigate(Routes.PRODUCTS) },
                    onGoCart = { navController.navigate(Routes.CART) },
                    onGoOrders = { navController.navigate(Routes.ORDERS) },
                    onOpenOrder = { orderId ->
                        navController.navigate("${Routes.ORDER_DETAILS}/$orderId")
                    }
                )
            }

            composable(Routes.PRODUCTS) { ProductScreen() }

            composable(Routes.CART) {
                CartScreen(
                    onCheckoutDone = { navController.navigate(Routes.ORDERS) }
                )
            }

            composable(Routes.ORDERS) {
                OrderHistoryScreen(
                    onOpenOrder = { id -> navController.navigate("${Routes.ORDER_DETAILS}/$id") }
                )
            }

            composable("${Routes.ORDER_DETAILS}/{orderId}") { entry ->
                val orderId = entry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
                OrderDetailsScreen(orderId = orderId, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.DASHBOARD,
            onClick = { onNavigate(Routes.DASHBOARD) },
            icon = { Icon(Icons.Filled.SpaceDashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PRODUCTS,
            onClick = { onNavigate(Routes.PRODUCTS) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Products") },
            label = { Text("Products") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.CART,
            onClick = { onNavigate(Routes.CART) },
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart") },
            label = { Text("Cart") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.ORDERS,
            onClick = { onNavigate(Routes.ORDERS) },
            icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = "Orders") },
            label = { Text("Orders") }
        )
    }
}
