package com.example.proyect_final.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyect_final.ui.screens.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyect_final.ui.viewmodel.AuthViewModel
import com.example.proyect_final.ui.viewmodel.CartViewModel

import androidx.compose.ui.Modifier
import androidx.compose.animation.*
import androidx.compose.animation.core.*

@Composable
fun NavGraph(
    navController: NavHostController,
    cartViewModel: CartViewModel, // Share this viewmodel across screens
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350, easing = EaseInOutCubic)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350, easing = EaseInOutCubic)
            ) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350, easing = EaseInOutCubic)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350, easing = EaseInOutCubic)
            ) + fadeOut(animationSpec = tween(350))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onSearchClick = {
                    navController.navigate(Screen.Catalog.route)
                },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }
        composable(Screen.Catalog.route) {
            CatalogScreen(
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                }
            )
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(animationSpec = tween(250))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ) + fadeOut(animationSpec = tween(250))
            }
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onProductClick = { id ->
                    navController.navigate(Screen.ProductDetail.createRoute(id))
                },
                onAdvisorClick = { id ->
                    navController.navigate(Screen.StyleAdvisor.createRoute(id.toString()))
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route) {
                        popUpTo(Screen.Home.route) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                cartViewModel = cartViewModel
            )
        }
        composable(
            route = Screen.StyleAdvisor.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            enterTransition = {
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(250)
                ) + fadeOut(animationSpec = tween(250))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250))
            },
            popExitTransition = {
                scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(250)
                ) + fadeOut(animationSpec = tween(250))
            }
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            StyleAdvisorScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onProductClick = { id ->
                    navController.navigate(Screen.ProductDetail.createRoute(id)) {
                        popUpTo(Screen.StyleAdvisor.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(viewModel = cartViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
    }
}
