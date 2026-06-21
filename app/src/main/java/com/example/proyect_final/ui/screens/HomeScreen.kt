package com.example.proyect_final.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.proyect_final.ui.viewmodel.HomeState
import com.example.proyect_final.ui.viewmodel.HomeViewModel
import com.example.proyect_final.ui.theme.CormorantFont
import com.example.proyect_final.ui.theme.MontserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit, // Reused as Shop Catalog Navigation Callback
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Section with status bar overlap (editorial look)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1539109136881-3be0616acf4b?q=80&w=1200", // Tall elegant model shot
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Vignette gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    
                    // Brand top bar inside Hero Box (no longer floating/pinned over other screen contents)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ELARA",
                            fontFamily = CormorantFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = 8.sp,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "COLECCIÓN E/I 2026",
                            fontFamily = MontserratFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 3.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "La Elegancia\nde lo Esencial",
                            fontFamily = CormorantFont,
                            fontWeight = FontWeight.Light,
                            fontSize = 42.sp,
                            color = Color.White,
                            lineHeight = 48.sp,
                            letterSpacing = (-0.5).sp
                        )
                        
                        Text(
                            text = "IA Sastre: Prendas recomendadas de manera inteligente para tu estilo único y sofisticado.",
                            fontFamily = MontserratFont,
                            fontWeight = FontWeight.Light,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(vertical = 16.dp),
                            lineHeight = 20.sp
                        )
                        
                        Button(
                            onClick = onSearchClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(0.dp), // Luxury sharp edges
                            modifier = Modifier.height(48.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp)
                        ) {
                            Text(
                                text = "DESCUBRIR TIENDA",
                                fontFamily = MontserratFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 1. Editorial Category Rows
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "SECCIONES EDITORIALES",
                        fontFamily = MontserratFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        val collections = listOf(
                            Triple("SASTRERÍA", "https://images.unsplash.com/photo-1544441893-675973e31985?q=80&w=1000", "Pantalones"),
                            Triple("ELEVADOS", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=1000", "Polos"),
                            Triple("MINIMALISTA", "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=1000", "Poleras"),
                            Triple("ACCESORIOS", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?q=80&w=1000", "Todos")
                        )
                        
                        items(collections) { (title, url, filter) ->
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSearchClick() }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                            )
                                        )
                                )
                                Text(
                                    text = title,
                                    fontFamily = CormorantFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 2. Buscador de Ropa IA (AI Searcher)
                var aiSearchQuery by remember { mutableStateOf("") }
                val aiSearchResult by viewModel.aiSearchResult.collectAsState()
                val aiSearchLoading by viewModel.aiSearchLoading.collectAsState()
                val aiSearchError by viewModel.aiSearchError.collectAsState()

                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "BÚSQUEDA INTELIGENTE IA",
                                    fontFamily = MontserratFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "Encuentra tu Prenda Ideal",
                                    fontFamily = CormorantFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "Pregúntale a nuestra IA Elara qué prenda estás buscando hoy (ej. 'un polo minimalista blanco y fresco' o 'saco elegante para fiesta') y la IA lo buscará en nuestro catálogo.",
                            fontFamily = MontserratFont,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp),
                            lineHeight = 18.sp
                        )

                        OutlinedTextField(
                            value = aiSearchQuery,
                            onValueChange = { aiSearchQuery = it },
                            placeholder = { 
                                Text(
                                    text = "¿Qué ropa estás buscando?",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (aiSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        aiSearchQuery = ""
                                        viewModel.clearAiSearch()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.searchProductsWithAI(aiSearchQuery) },
                            enabled = aiSearchQuery.trim().isNotEmpty() && !aiSearchLoading,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (aiSearchLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "BUSCAR CON ELARA IA",
                                        fontFamily = MontserratFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Displaying AI Search Results
                        aiSearchResult?.let { results ->
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (results.isEmpty()) {
                                Text(
                                    text = "No encontramos prendas exactas que coincidan con tu búsqueda. ¡Intenta con otras palabras!",
                                    fontFamily = MontserratFont,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                )
                            } else {
                                Text(
                                    text = "RESULTADOS ENCONTRADOS:",
                                    fontFamily = MontserratFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(results) { product ->
                                        val productInteractionSource = remember { MutableInteractionSource() }
                                        val isProductPressed by productInteractionSource.collectIsPressedAsState()
                                        
                                        val prodScale by animateFloatAsState(
                                            targetValue = if (isProductPressed) 0.96f else 1.0f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                            label = "prod_scale"
                                        )
                                        val prodImgOffsetY by animateDpAsState(
                                            targetValue = if (isProductPressed) (-8).dp else 0.dp,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                            label = "prod_img_offset"
                                        )
                                        val prodImgScale by animateFloatAsState(
                                            targetValue = if (isProductPressed) 1.08f else 1.0f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                            label = "prod_img_scale"
                                        )

                                        Card(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .graphicsLayer(scaleX = prodScale, scaleY = prodScale)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable(
                                                    interactionSource = productInteractionSource,
                                                    indication = androidx.compose.foundation.LocalIndication.current,
                                                    onClick = { onProductClick(product.id) }
                                                ),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(120.dp)
                                                        .background(Color(0xFFF7F7F7))
                                                ) {
                                                    AsyncImage(
                                                        model = com.example.proyect_final.data.remote.getProductImageModel(product.image),
                                                        contentDescription = product.title,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(8.dp)
                                                            .offset(y = prodImgOffsetY)
                                                            .scale(prodImgScale),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(
                                                        text = product.title,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "S/ ${product.price}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 3. Tendencias Ahora Grid
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "TENDENCIAS AHORA",
                        fontFamily = MontserratFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Selección exclusiva curada por nuestro motor de IA",
                        fontFamily = CormorantFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )
                    
                    if (uiState is HomeState.Success) {
                        val allProducts = (uiState as HomeState.Success).products
                        val trendProducts = allProducts.take(4) // Display top 4 trending items
                        
                        Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                            trendProducts.chunked(2).forEach { rowProducts ->
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    rowProducts.forEach { product ->
                                        val interactionSource = remember { MutableInteractionSource() }
                                        val isPressed by interactionSource.collectIsPressedAsState()
                                        
                                        val cardScale by animateFloatAsState(
                                            targetValue = if (isPressed) 0.96f else 1.0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            ),
                                            label = "card_scale"
                                        )
                                        val imgOffsetY by animateDpAsState(
                                            targetValue = if (isPressed) (-10).dp else 0.dp,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            label = "img_offset"
                                        )
                                        val imgScale by animateFloatAsState(
                                            targetValue = if (isPressed) 1.08f else 1.0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            label = "img_scale"
                                        )

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .graphicsLayer(scaleX = cardScale, scaleY = cardScale)
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = androidx.compose.foundation.LocalIndication.current,
                                                    onClick = { onProductClick(product.id) }
                                                )
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(260.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                            ) {
                                                AsyncImage(
                                                    model = com.example.proyect_final.data.remote.getProductImageModel(product.image),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .offset(y = imgOffsetY)
                                                        .scale(imgScale),
                                                    contentScale = ContentScale.Crop
                                                )
                                                
                                                Surface(
                                                    color = Color.White.copy(alpha = 0.75f),
                                                    shape = CircleShape,
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(10.dp)
                                                        .size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Share,
                                                        contentDescription = null,
                                                        modifier = Modifier.padding(8.dp),
                                                        tint = Color.Black
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            
                                            Text(
                                                text = product.brand.uppercase(),
                                                fontFamily = MontserratFont,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 1.sp
                                            )
                                            
                                            Text(
                                                text = product.title,
                                                fontFamily = CormorantFont,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                            
                                            Text(
                                                text = "S/ ${product.price}",
                                                fontFamily = MontserratFont,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                    if (rowProducts.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(56.dp))

                // 4. Concierge Digital (Email Subscription Box)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CONCIERGE DIGITAL",
                        fontFamily = MontserratFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Recibe reportes de estilo curados y acceso prioritario a lanzamientos limitados.",
                        fontFamily = CormorantFont,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                        lineHeight = 22.sp
                    )
                    
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        placeholder = { 
                            Text(
                                text = "Tu correo electrónico",
                                fontFamily = MontserratFont,
                                fontWeight = FontWeight.Light,
                                fontSize = 13.sp,
                                color = Color.Gray
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF111111),
                            focusedContainerColor = Color(0xFF111111),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedBorderColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text(
                            text = "SUSCRIBIRME",
                            fontFamily = MontserratFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}
