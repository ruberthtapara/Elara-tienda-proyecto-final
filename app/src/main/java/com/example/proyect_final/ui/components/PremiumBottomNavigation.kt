package com.example.proyect_final.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.proyect_final.ui.navigation.Screen
import com.example.proyect_final.ui.viewmodel.CartViewModel
import kotlin.math.roundToInt

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

enum class DockPosition {
    BOTTOM, LEFT, RIGHT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumBottomNavigation(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide navigation dock on login and product detail screens to prevent blocking the purchase button
    if (currentRoute == Screen.Login.route || currentRoute?.startsWith("product_detail") == true) {
        return
    }

    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    val items = listOf(
        BottomNavItem("Inicio", Screen.Home.route, Icons.Outlined.Home, Icons.Filled.Home),
        BottomNavItem("Tienda", Screen.Catalog.route, Icons.Outlined.Storefront, Icons.Filled.Storefront),
        BottomNavItem("Carrito", Screen.Cart.route, Icons.Outlined.LocalMall, Icons.Filled.LocalMall),
        BottomNavItem("Cuenta", Screen.Profile.route, Icons.Outlined.Person, Icons.Filled.Person)
    )

    var dockPosition by remember { mutableStateOf(DockPosition.BOTTOM) }

    LaunchedEffect(currentRoute) {
        if (currentRoute?.contains("style_advisor", ignoreCase = true) == true) {
            dockPosition = DockPosition.LEFT
        } else {
            dockPosition = DockPosition.BOTTOM
        }
    }

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Dimension states
    val targetWidth = if (dockPosition == DockPosition.BOTTOM) 280.dp else 64.dp
    val targetHeight = if (dockPosition == DockPosition.BOTTOM) 64.dp else 280.dp

    val animatedWidth by animateDpAsState(targetValue = targetWidth, animationSpec = spring(stiffness = Spring.StiffnessLow))
    val animatedHeight by animateDpAsState(targetValue = targetHeight, animationSpec = spring(stiffness = Spring.StiffnessLow))

    // Base position offset calculations
    val targetX = when (dockPosition) {
        DockPosition.BOTTOM -> (screenWidth - 280.dp) / 2
        DockPosition.LEFT -> 12.dp
        DockPosition.RIGHT -> screenWidth - 64.dp - 12.dp
    }
    
    val targetY = when (dockPosition) {
        DockPosition.BOTTOM -> screenHeight - 64.dp - 32.dp
        DockPosition.LEFT, DockPosition.RIGHT -> (screenHeight - 280.dp) / 2
    }

    val animatedTargetX by animateDpAsState(
        targetValue = targetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val animatedTargetY by animateDpAsState(
        targetValue = targetY,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    // Drag offset animations
    val dragOffsetDpX = with(density) { dragOffset.x.toDp() }
    val dragOffsetDpY = with(density) { dragOffset.y.toDp() }

    val animatedDragX by animateDpAsState(
        targetValue = if (isDragging) dragOffsetDpX else 0.dp,
        animationSpec = if (isDragging) snap() else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )
    val animatedDragY by animateDpAsState(
        targetValue = if (isDragging) dragOffsetDpY else 0.dp,
        animationSpec = if (isDragging) snap() else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    val currentX = animatedTargetX + animatedDragX
    val currentY = animatedTargetY + animatedDragY

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main floating navigation dock
        Surface(
            modifier = Modifier
                .offset(x = currentX, y = currentY)
                .size(width = animatedWidth, height = animatedHeight)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black
                )
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    RoundedCornerShape(32.dp)
                ),
            color = Color.Black.copy(alpha = 0.92f), // Luxury dark glass style
            shape = RoundedCornerShape(32.dp)
        ) {
            val contentModifier = Modifier
                .padding(
                    horizontal = if (dockPosition == DockPosition.BOTTOM) 12.dp else 8.dp,
                    vertical = if (dockPosition == DockPosition.BOTTOM) 8.dp else 12.dp
                )

            Crossfade(
                targetState = dockPosition == DockPosition.BOTTOM,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "dock_layout"
            ) { isBottom ->
                if (isBottom) {
                    Row(
                        modifier = contentModifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drag handle
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "Arrastrar para mover",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(24.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { isDragging = true },
                                        onDragEnd = {
                                            isDragging = false
                                            val fingerX = targetX + dragOffsetDpX
                                            val fingerY = targetY + dragOffsetDpY
                                            
                                            // Snap to nearest edge position
                                            dockPosition = when {
                                                fingerX < 120.dp -> DockPosition.LEFT
                                                fingerX > (screenWidth - 120.dp) -> DockPosition.RIGHT
                                                else -> DockPosition.BOTTOM
                                            }
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount
                                        }
                                    )
                                }
                        )

                        DockItems(items, currentRoute, navController, cartCount, isVertical = false)
                        
                        // Repositioning helper button
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Posición",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { dockPosition = DockPosition.LEFT }
                        )
                    }
                } else {
                    Column(
                        modifier = contentModifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drag handle
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "Arrastrar para mover",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(24.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { isDragging = true },
                                        onDragEnd = {
                                            isDragging = false
                                            val fingerX = targetX + dragOffsetDpX
                                            val fingerY = targetY + dragOffsetDpY
                                            
                                            // Snap to nearest edge position
                                            dockPosition = when {
                                                fingerX < 120.dp -> DockPosition.LEFT
                                                fingerX > (screenWidth - 120.dp) -> DockPosition.RIGHT
                                                else -> DockPosition.BOTTOM
                                            }
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount
                                        }
                                    )
                                }
                        )

                        DockItems(items, currentRoute, navController, cartCount, isVertical = true)
                        
                        // Repositioning helper button
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Posición",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    dockPosition = if (dockPosition == DockPosition.LEFT) DockPosition.RIGHT else DockPosition.BOTTOM
                                }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DockItems(
    items: List<BottomNavItem>,
    currentRoute: String?,
    navController: NavController,
    cartCount: Int,
    isVertical: Boolean
) {
    items.forEach { item ->
        val isSelected = currentRoute?.startsWith(item.route.split("/")[0]) == true

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color.White.copy(alpha = 0.12f)
                    else Color.Transparent
                )
                .clickable {
                    if (!isSelected) {
                        val isCoreTab = currentRoute == Screen.Home.route || 
                                        currentRoute == Screen.Catalog.route || 
                                        currentRoute == Screen.Cart.route || 
                                        currentRoute == Screen.Profile.route

                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { 
                                saveState = isCoreTab 
                            }
                            launchSingleTop = true
                            restoreState = isCoreTab
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (item.route == Screen.Cart.route) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            var triggerAnimation by remember { mutableStateOf(false) }
                            LaunchedEffect(cartCount) {
                                triggerAnimation = true
                                kotlinx.coroutines.delay(300)
                                triggerAnimation = false
                            }
                            
                            val badgeScale by animateFloatAsState(
                                targetValue = if (triggerAnimation) 1.4f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "cart_badge_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .defaultMinSize(minWidth = 10.dp, minHeight = 10.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .scale(badgeScale)
                                    .padding(horizontal = 1.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 7.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.icon,
                    contentDescription = item.title,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
