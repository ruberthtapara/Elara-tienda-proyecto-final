package com.example.proyect_final.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.proyect_final.ui.viewmodel.HomeState
import com.example.proyect_final.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var selectedSubcategoryIndex by remember { mutableIntStateOf(0) }

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    // Filter states
    var selectedGender by remember { mutableStateOf("Todos") }
    var selectedBrand by remember { mutableStateOf("Todas") }
    var selectedSortOrder by remember { mutableStateOf("Relevancia") }
    var priceRange by remember { mutableStateOf(0f..500f) }
    var selectedSizeFilter by remember { mutableStateOf("Todas") }

    val categoryPairs = listOf(
        "Todos" to "Todos",
        "Pantalones" to "Pantalones",
        "Polos" to "Polos",
        "Poleras" to "Poleras",
        "Chaquetas" to "Chaquetas",
        "Vestidos" to "Vestidos",
        "Accesorios" to "Accesorios",
        "Conjunto completo" to "Conjunto completo"
    )

    val subcategories = listOf(
        "Todos" to "Todos",
        "Sombreros" to "Sombreros",
        "Bolsos" to "Bolsos",
        "Gafas" to "Gafas",
        "Relojes" to "Relojes",
        "Collares" to "Collares"
    )

    LaunchedEffect(selectedCategoryIndex) {
        selectedSubcategoryIndex = 0
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAllProducts()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "ELARA STORE", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is HomeState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is HomeState.Success -> {
                    // Dynamic brands list from available products
                    val brands = remember(state.products) {
                        listOf("Todas") + state.products.map { it.brand }.distinct().sorted()
                    }
                    val sizes = listOf("Todas", "S", "M", "L", "XL")
                    val sortOrders = listOf("Relevancia", "Precio: Menor a Mayor", "Precio: Mayor a Menor")
                    val genders = listOf("Todos", "Hombre", "Mujer", "Unisex")

                    // Filter products locally based on Search Bar and Category Chips + Advanced Filters
                    val filteredProducts = state.products.filter { product ->
                        val matchesSearch = product.title.contains(searchQuery, ignoreCase = true) || 
                                            product.brand.contains(searchQuery, ignoreCase = true) ||
                                            product.description.contains(searchQuery, ignoreCase = true)
                        
                        val categoryKey = categoryPairs[selectedCategoryIndex].second
                        val matchesCategory = categoryKey == "Todos" || 
                                             product.category.equals(categoryKey, ignoreCase = true)
                        
                        val matchesSubcategory = if (categoryKey.equals("Accesorios", ignoreCase = true)) {
                            val subcategoryKey = subcategories[selectedSubcategoryIndex].second
                            subcategoryKey == "Todos" || product.subcategory.equals(subcategoryKey, ignoreCase = true)
                        } else {
                            true
                        }
                        
                        val matchesGender = selectedGender == "Todos" || 
                                           product.gender.equals(selectedGender, ignoreCase = true)
                        
                        val matchesBrand = selectedBrand == "Todas" || 
                                          product.brand.equals(selectedBrand, ignoreCase = true)
                        
                        val matchesSize = selectedSizeFilter == "Todas" || 
                                         product.sizes.any { it.equals(selectedSizeFilter, ignoreCase = true) }
                        
                        val matchesPrice = product.price >= priceRange.start && product.price <= priceRange.endInclusive
                        
                        matchesSearch && matchesCategory && matchesSubcategory && matchesGender && matchesBrand && matchesSize && matchesPrice
                    }.let { list ->
                        when (selectedSortOrder) {
                            "Precio: Menor a Mayor" -> list.sortedBy { it.price }
                            "Precio: Mayor a Menor" -> list.sortedByDescending { it.price }
                            else -> list
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 96.dp)
                    ) {
                        // 1. Search Bar & Filter Button (Spans full width)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape),
                                    placeholder = { Text("Buscar en Elara...", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = CircleShape,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                IconButton(
                                    onClick = { showFilterSheet = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (selectedGender != "Todos" || selectedBrand != "Todas" || selectedSortOrder != "Relevancia" || priceRange != 0f..500f || selectedSizeFilter != "Todas") {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            },
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filtros",
                                        tint = if (selectedGender != "Todos" || selectedBrand != "Todas" || selectedSortOrder != "Relevancia" || priceRange != 0f..500f || selectedSizeFilter != "Todas") {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Horizontal Category Chips (Spans full width)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categoryPairs.size) { index ->
                                    val isSelected = index == selectedCategoryIndex
                                    val categoryName = categoryPairs[index].first
                                    
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategoryIndex = index },
                                        label = { Text(categoryName) },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = null
                                    )
                                }
                            }
                        }

                        // Horizontal Subcategory Chips for Accesorios (Spans full width)
                        if (categoryPairs[selectedCategoryIndex].second.equals("Accesorios", ignoreCase = true)) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(subcategories.size) { index ->
                                        val isSelected = index == selectedSubcategoryIndex
                                        val subcategoryName = subcategories[index].first
                                        
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedSubcategoryIndex = index },
                                            label = { Text(subcategoryName) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            border = null
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Horizontal Lookbook / Inspiración Row (Spans full width)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = "LOOKBOOK — INSPIRACIÓN DIARIA",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                                )
                                
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth().height(260.dp)
                                ) {
                                    val lookbooks = listOf(
                                        Pair("https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=600", "Casual Urbano"),
                                        Pair("https://images.unsplash.com/photo-1488161628813-04466f872be2?q=80&w=600", "Sastrería Moderna"),
                                        Pair("https://images.unsplash.com/photo-1529139574466-a303027c1d8b?q=80&w=600", "Minimalista Chic"),
                                        Pair("https://images.unsplash.com/photo-1509631179647-0177331693ae?q=80&w=600", "Alta Costura"),
                                        Pair("https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=600", "Estilo Nórdico")
                                    )
                                    
                                    items(lookbooks.size) { index ->
                                        val item = lookbooks[index]
                                        Box(
                                            modifier = Modifier
                                                .width(180.dp)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable { /* Opcional: ver look o filtrar */ }
                                        ) {
                                            AsyncImage(
                                                model = item.first,
                                                contentDescription = item.second,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            // Bottom gradient
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                                            startY = 180f
                                                        )
                                                    )
                                            )
                                            // Label overlay
                                            Text(
                                                text = item.second.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section Title (Spans full width)
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Nuestras Colecciones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                            )
                        }

                        // 4. Grid of products
                        if (filteredProducts.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "No se encontraron resultados",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Prueba con otra palabra clave o cambia el filtro de categoría.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(filteredProducts) { product ->
                                ElaraProductCard(
                                    product = product,
                                    isFavorite = favoriteIds.contains(product.id),
                                    onFavoriteToggle = { viewModel.toggleFavorite(product.id) },
                                    onClick = onProductClick
                                )
                            }
                        }
                    }

                    if (showFilterSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showFilterSheet = false },
                            sheetState = sheetState,
                            dragHandle = { BottomSheetDefaults.DragHandle() },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 32.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FILTRAR Y ORDENAR",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    TextButton(
                                        onClick = {
                                            selectedGender = "Todos"
                                            selectedBrand = "Todas"
                                            selectedSortOrder = "Relevancia"
                                            priceRange = 0f..500f
                                            selectedSizeFilter = "Todas"
                                        }
                                    ) {
                                        Text(
                                            text = "Limpiar Todo",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                
                                // 1. Sort Order
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "ORDENAR POR",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        sortOrders.forEach { order ->
                                            val isSelected = order == selectedSortOrder
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedSortOrder = order },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = order,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // 2. Gender
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "GÉNERO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        genders.forEach { gender ->
                                            val isSelected = gender == selectedGender
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedGender = gender },
                                                label = { Text(gender, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = isSelected,
                                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                                    selectedBorderColor = Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }
                                
                                // 3. Price Range
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "RANGO DE PRECIO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "S/ ${priceRange.start.toInt()} - S/ ${priceRange.endInclusive.toInt()}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    RangeSlider(
                                        value = priceRange,
                                        onValueChange = { priceRange = it },
                                        valueRange = 0f..500f,
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    )
                                }
                                
                                // 4. Size Filter
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "TALLA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(sizes.size) { index ->
                                            val size = sizes[index]
                                            val isSelected = size == selectedSizeFilter
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedSizeFilter = size },
                                                label = { Text(size, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = isSelected,
                                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                                    selectedBorderColor = Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }

                                // 5. Brand Filter
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "MARCA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(brands.size) { index ->
                                            val brand = brands[index]
                                            val isSelected = brand == selectedBrand
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedBrand = brand },
                                                label = { Text(brand, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = isSelected,
                                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                                    selectedBorderColor = Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(
                                    onClick = { showFilterSheet = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        text = "VER RESULTADOS (${filteredProducts.size})",
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
                is HomeState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Error al conectar con el catálogo real")
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.getProducts() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ElaraProductCard(
    product: com.example.proyect_final.domain.model.Product,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
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

    Card(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = { onClick(product.id) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .graphicsLayer(translationY = imgOffsetY.value, scaleX = imgScale, scaleY = imgScale),
                    contentScale = ContentScale.Fit
                )

                // Category Tag Top-Left (Smaller, premium light-blue badge showing the actual category)
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE1F5FE))
                        .padding(horizontal = 6.dp, vertical = 2.5.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = Color(0xFF01579B),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favorite Button Top-Right
                IconButton(
                    onClick = { onFavoriteToggle() },
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.9f), shape = CircleShape)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "4.8",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
