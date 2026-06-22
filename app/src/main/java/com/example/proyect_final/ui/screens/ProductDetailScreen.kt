package com.example.proyect_final.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.proyect_final.ui.viewmodel.DetailState
import com.example.proyect_final.ui.viewmodel.ProductDetailViewModel
import com.example.proyect_final.ui.viewmodel.CartViewModel
import com.example.proyect_final.ui.viewmodel.ProductReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onAdvisorClick: (String) -> Unit,
    onCartClick: () -> Unit,
    cartViewModel: CartViewModel,
    viewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModel.provideFactory(productId))
) {
    val uiState by viewModel.uiState.collectAsState()
    val recommendedProducts by viewModel.recommendedProducts.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val complementaryAccessories by viewModel.complementaryAccessories.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    var isAddingToCart by remember { mutableStateOf(false) }
    var isAddedToCart by remember { mutableStateOf(false) }
    
    val animProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    var isFlyingToCart by remember { mutableStateOf(false) }
    var pulseCartIcon by remember { mutableStateOf(false) }
    val cartScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pulseCartIcon) 1.4f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "cart_scale"
    )
    
    var selectedSize by remember { mutableStateOf("M") }

    var isReviewsExpanded by remember { mutableStateOf(false) }
    var isShippingExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is DetailState.Success) {
            val product = (uiState as DetailState.Success).product
            if (product.sizes.isNotEmpty() && !product.sizes.contains(selectedSize)) {
                selectedSize = product.sizes.first()
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (uiState is DetailState.Success) {
                val product = (uiState as DetailState.Success).product
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive Favorite Button next to checkout
                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (isFavorite) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Button(
                            onClick = {
                                if (!isAddingToCart && !isAddedToCart) {
                                    isAddingToCart = true
                                    cartViewModel.addProduct(product, selectedSize)
                                    coroutineScope.launch {
                                        // Trigger flight animation
                                        isFlyingToCart = true
                                        animProgress.snapTo(0f)
                                        animProgress.animateTo(
                                            targetValue = 1f,
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = 800,
                                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                                            )
                                        )
                                        isFlyingToCart = false
                                        
                                        // Trigger cart icon pulse animation
                                        pulseCartIcon = true
                                        isAddingToCart = false
                                        isAddedToCart = true
                                        kotlinx.coroutines.delay(200)
                                        pulseCartIcon = false
                                        kotlinx.coroutines.delay(1600)
                                        isAddedToCart = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isAddedToCart -> Color(0xFF10B981) // Green success
                                    isAddingToCart -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            val buttonText = when {
                                isAddingToCart -> "Añadiendo..."
                                isAddedToCart -> "¡Añadido!"
                                else -> "Añadir al Carrito"
                            }
                            
                            AnimatedContent(
                                targetState = buttonText,
                                label = "cart_button_anim",
                                transitionSpec = {
                                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                                }
                            ) { text ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isAddingToCart) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else if (isAddedToCart) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    
                                    Text(
                                        text = text.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    
                                    if (!isAddingToCart && !isAddedToCart) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "— S/ ${product.price}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Light,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is DetailState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DetailState.Success -> {
                    val product = state.product
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Reso-enhanced Image Header - using ContentScale.Fit centered in high contrast background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = com.example.proyect_final.data.remote.getProductImageModel(product.image),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                                    .padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                            
                            // Bottom Vignette Gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                                        )
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(top = 16.dp)
                        ) {
                            // Section header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COLECCIÓN ${product.season.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    letterSpacing = 1.5.sp
                                )
                                if (product.stock > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "Stock: ${product.stock} un.",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            
                            // Title & Price Row
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = product.brand,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "Género: ${product.gender}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Text(
                                    text = "S/ ${product.price}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 20.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            
                            // Description
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Size Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Seleccionar Talla".uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Guía de tallas",
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.Underline,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { }
                                )
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                val sizesToDisplay = if (product.sizes.isNotEmpty()) product.sizes else listOf("S", "M", "L")
                                sizesToDisplay.forEach { size ->
                                    val isSelected = selectedSize == size
                                    
                                    val scale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.15f else 1.0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "size_scale"
                                    )
                                    val backgroundColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        animationSpec = tween(durationMillis = 200),
                                        label = "size_bg"
                                    )
                                    val contentColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        animationSpec = tween(durationMillis = 200),
                                        label = "size_content"
                                    )
                                    val borderColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                                        animationSpec = tween(durationMillis = 200),
                                        label = "size_border"
                                    )
                                    val elevation by animateDpAsState(
                                        targetValue = if (isSelected) 6.dp else 2.dp,
                                        animationSpec = tween(durationMillis = 200),
                                        label = "size_elevation"
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .scale(scale)
                                            .clickable { selectedSize = size },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = borderColor
                                        ),
                                        color = backgroundColor,
                                        shadowElevation = elevation
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = size,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = contentColor
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (!product.category.equals("Conjunto completo", ignoreCase = true)) {
                                // Futuristic Gradient AI Combination Button
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { onAdvisorClick(product.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "VER COMBINACIONES CON IA",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            letterSpacing = 1.5.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            // IA Accessories Section
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(8.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "IA DE ACCESORIOS COMPLEMENTARIOS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    
                                    Text(
                                        text = "Completa el look sugerido",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        complementaryAccessories.forEach { accessory ->
                                            AccessoryDetailCard(
                                                title = accessory.title,
                                                price = accessory.price.toInt(),
                                                img = accessory.image,
                                                onClick = { onProductClick(accessory.id) }
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    OutlinedButton(
                                        onClick = { },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = CircleShape,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Consultar con Estilista Personal", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Expandable Sections
                            ExpandableSection(
                                title = "Reseñas",
                                isExpanded = isReviewsExpanded,
                                onToggle = { isReviewsExpanded = !isReviewsExpanded }
                            ) {
                                val context = LocalContext.current
                                val currentUser by viewModel.currentUserState.collectAsState()
                                ReviewsContent(
                                    reviews = reviews,
                                    isLoggedIn = currentUser != null,
                                    onSubmitReview = { rating, comment ->
                                        viewModel.addReview(rating, comment) { result ->
                                            result.onSuccess {
                                                Toast.makeText(context, "Reseña publicada con éxito", Toast.LENGTH_SHORT).show()
                                            }.onFailure { e ->
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                            
                            ExpandableSection(
                                title = "Envío y Devoluciones",
                                isExpanded = isShippingExpanded,
                                onToggle = { isShippingExpanded = !isShippingExpanded }
                            ) {
                                ShippingContent()
                            }

                            // Recomendados Section
                            if (recommendedProducts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    text = "PRODUCTOS RECOMENDADOS",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(recommendedProducts) { recProduct ->
                                        RecommendedProductCard(
                                            product = recProduct,
                                            onClick = { onProductClick(recProduct.id) }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
                is DetailState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Error al cargar el producto", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.getProduct() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
            
            // Floating Top Bar Overlaying Image
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "ELARA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(cartScale)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalMall,
                        contentDescription = "Carrito",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Add to Cart Fly Animation Overlay
            if (isFlyingToCart && uiState is DetailState.Success) {
                val product = (uiState as DetailState.Success).product
                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val screenWidthDp = configuration.screenWidthDp.dp
                
                // Center coordinates of the flight path (parabolic throw)
                val startX = screenWidthDp / 2
                val startY = 240.dp // center of the product image area
                val endX = screenWidthDp - 36.dp // center of the top-right cart icon
                val endY = 52.dp // center of the top-right cart icon
                
                val progress = animProgress.value
                val arcHeight = 160.dp
                
                // Calculate current center using parabolic curve
                val centerX = startX + (endX - startX) * progress
                val centerY = startY + (endY - startY) * progress - (arcHeight.value * kotlin.math.sin(progress * Math.PI).toFloat()).dp
                
                // Shrink from 80.dp down to 18.dp to simulate flying away/into the cart
                val currentSize = 80.dp - (62.dp * progress)
                
                // Top-left offset based on the centered coordinates
                val currentX = centerX - (currentSize / 2)
                val currentY = centerY - (currentSize / 2)
                
                // Fade out rapidly in the last 20% of the trajectory
                val alpha = if (progress < 0.8f) 1f else (1f - progress) / 0.2f
                
                Box(
                    modifier = Modifier
                        .offset(x = currentX, y = currentY)
                        .size(currentSize)
                        .graphicsLayer(alpha = alpha)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .background(Color.White)
                ) {
                    AsyncImage(
                        model = com.example.proyect_final.data.remote.getProductImageModel(product.image),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun AccessoryDetailCard(title: String, price: Int, img: String, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = com.example.proyect_final.data.remote.getProductImageModel(img),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Surface(
                color = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Icon(Icons.Default.LocalMall, contentDescription = null, modifier = Modifier.padding(4.dp), tint = Color.Black)
            }
        }
        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp), maxLines = 1)
        Text("S/ $price", fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        androidx.compose.animation.AnimatedVisibility(
            visible = isExpanded,
            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ReviewsContent(
    reviews: List<ProductReview>,
    isLoggedIn: Boolean,
    onSubmitReview: (Double, String) -> Unit
) {
    // State for the review form
    var userRating by remember { mutableStateOf(5.0) }
    var commentText by remember { mutableStateOf("") }
    
    // Average rating calculations
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else {
        5.0
    }
    
    val displayRatingStr = String.format(Locale.US, "%.1f", averageRating)
    val totalReviews = reviews.size
    
    val recommendPercent = if (reviews.isNotEmpty()) {
        val positive = reviews.count { it.rating >= 4.0 }
        (positive * 100) / reviews.size
    } else {
        100
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary stats header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayRatingStr,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "de 5 estrellas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$recommendPercent% de clientes recomiendan esta prenda",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Basado en $totalReviews ${if (totalReviews == 1) "opinión real" else "opiniones reales"} de compradores.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        
        // Form to write a review
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ESCRIBE TU RESEÑA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                
                if (isLoggedIn) {
                    // Star Rating selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Calificación: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 1..5) {
                                val ratingVal = i.toDouble()
                                val isSelected = ratingVal <= userRating
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$i Estrellas",
                                    tint = if (isSelected) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable { userRating = ratingVal }
                                )
                            }
                        }
                    }
                    
                    // Comment input
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Escribe tu comentario aquí sobre la calidad, talla, tela...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        maxLines = 4,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onSubmitReview(userRating, commentText)
                                commentText = ""
                                userRating = 5.0
                            }
                        },
                        enabled = commentText.isNotBlank(),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Publicar Reseña", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Inicia sesión con una cuenta para poder dejar una reseña sobre esta prenda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        
        // Reviews List
        if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay reseñas para este producto. ¡Sé el primero en dejar una!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "PE")) }
            reviews.forEach { review ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${review.userName} (Compra Verificada)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= review.rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sdf.format(Date(review.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = review.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun ShippingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                Text(
                    text = "Envío Express (Gratis desde S/ 150)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Entrega prioritaria a domicilio en 24 a 48 horas hábiles en Lima Metropolitana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                Text(
                    text = "Envío Regular (S/ 10.00)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Entrega estándar de 3 a 5 días hábiles a nivel nacional en todo el Perú.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                Text(
                    text = "Devoluciones Gratuitas (Hasta 30 días)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "¿No te convence la talla o el estilo? Realiza tu devolución de forma 100% gratuita a través de nuestros canales de atención en los primeros 30 días calendario tras recibir tu producto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecommendedProductCard(
    product: com.example.proyect_final.domain.model.Product,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = com.example.proyect_final.data.remote.getProductImageModel(product.image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = product.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "S/ ${product.price}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
