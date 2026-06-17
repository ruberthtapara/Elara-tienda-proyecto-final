package com.example.proyect_final.ui.navigation
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Catalog : Screen("catalog")
    object StyleAdvisor : Screen("style_advisor/{productId}") {
        fun createRoute(productId: String) = "style_advisor/$productId"
    }
    object Cart : Screen("cart")
    object Profile : Screen("settings")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
}
