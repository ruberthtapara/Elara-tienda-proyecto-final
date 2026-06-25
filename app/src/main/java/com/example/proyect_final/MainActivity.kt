package com.example.proyect_final

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.proyect_final.ui.components.PremiumBottomNavigation
import com.example.proyect_final.ui.navigation.NavGraph
import com.example.proyect_final.ui.theme.Proyect_finalTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyect_final.ui.viewmodel.CartViewModel
import com.example.proyect_final.domain.model.UserPreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val application = (LocalContext.current.applicationContext as StyleGenApplication)
            val profileRepository = application.container.userProfileRepository
            val preferences by profileRepository.getUserPreferences().collectAsState(initial = UserPreferences())

            Proyect_finalTheme(colorPalette = preferences.colorPalette) {
                StyleGenApp()
            }
        }
    }

    /**
     * MODIFICADO PARA EL EJERCICIO FINAL:
     * Captura intents entrantes cuando la actividad está en segundo plano (singleTask/singleTop)
     * permitiendo que los botones del Widget redirijan de inmediato sin duplicar la actividad.
     */
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
@Composable
fun StyleGenApp() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModel.Factory)
    val context = LocalContext.current
    val activity = context as android.app.Activity
    val destination = activity.intent.getStringExtra("EXTRA_NAV_DESTINATION")
    androidx.compose.runtime.LaunchedEffect(destination) {
        if (destination != null) {
            val route = when (destination) {
                "cart" -> com.example.proyect_final.ui.navigation.Screen.Cart.route
                "catalog" -> com.example.proyect_final.ui.navigation.Screen.Catalog.route
                "advisor" -> com.example.proyect_final.ui.navigation.Screen.StyleAdvisor.createRoute("0")
                else -> null
            }
            if (route != null) {
                navController.navigate(route) {
                    popUpTo(com.example.proyect_final.ui.navigation.Screen.Home.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            activity.intent.removeExtra("EXTRA_NAV_DESTINATION")
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavGraph(
                navController = navController,
                cartViewModel = cartViewModel,
                modifier = Modifier.fillMaxSize()
            )
            
            PremiumBottomNavigation(
                navController = navController,
                cartViewModel = cartViewModel
            )
        }
    }
}
