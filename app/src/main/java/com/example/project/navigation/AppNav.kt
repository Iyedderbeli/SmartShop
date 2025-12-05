package com.example.project.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project.auth.LoginScreen
import com.example.project.auth.LoginViewModel
import com.example.project.products.ProductScreen

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                viewModel = LoginViewModel(),
                onLoginSuccess = { navController.navigate("home") }
            )
        }

        composable("home") {
            ProductScreen()
        }
    }
}
