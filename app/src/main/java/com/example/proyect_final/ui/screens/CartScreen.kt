package com.example.proyect_final.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.proyect_final.ui.viewmodel.CartViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: CartViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal = viewModel.subtotal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ELARA FASHION", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Text("Bolsa de Compra", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Text("${cartItems.size} artículos seleccionados", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DailyCheckInBoard(viewModel = viewModel)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (cartItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tu bolsa está vacía", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text("Explora nuestras colecciones para empezar", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                } else {
                    items(cartItems) { item ->
                        CartItemRow(
                            title = item.product.title,
                            variant = "${item.product.color} / ${item.size}",
                            price = item.product.price,
                            image = item.product.image,
                            quantity = item.quantity,
                            onRemove = { viewModel.removeProduct(item.product.id, item.size) },
                            onIncrease = { viewModel.updateQuantity(item.product.id, item.size, item.quantity + 1) },
                            onDecrease = { viewModel.updateQuantity(item.product.id, item.size, item.quantity - 1) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        SummarySection(viewModel = viewModel)
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    title: String, 
    variant: String, 
    price: Double, 
    image: String, 
    quantity: Int,
    onRemove: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier.size(100.dp).clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
                }
            }
            Text(variant, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(border = BorderStroke(1.dp, Color.LightGray), shape = CircleShape) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp).clickable { onDecrease() })
                        Text(quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp), fontSize = 12.sp)
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp).clickable { onIncrease() })
                    }
                }
                Text("$${String.format("%.2f", price * quantity)}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SummarySection(viewModel: CartViewModel) {
    val subtotal = viewModel.subtotal
    val discount = viewModel.discountAmount
    val total = viewModel.totalAmount
    val selectedCouponDay by viewModel.selectedCouponDay.collectAsState()
    val selectedCouponPercentage = viewModel.selectedCouponPercentage

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                SummaryRow("Subtotal", "$${String.format("%.2f", subtotal)}")
                SummaryRow("Envío", "Gratis", isSuccess = true)
                
                val claimedDaysCount by viewModel.claimedDaysCount.collectAsState()
                if (claimedDaysCount > 0) {
                    var showCouponDialog by remember { mutableStateOf(false) }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { showCouponDialog = true }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedCouponDay > 0) "Cupón de descuento (-$selectedCouponPercentage%)" else "Cupón de descuento",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (selectedCouponDay > 0) "-$${String.format("%.2f", discount)}" else "Seleccionar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCouponDay > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (selectedCouponDay > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (showCouponDialog) {
                        CouponSelectionDialog(
                            viewModel = viewModel,
                            onDismiss = { showCouponDialog = false }
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cupón de descuento",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        Text(
                            text = "Sin cupones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("$${String.format("%.2f", total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val context = LocalContext.current
        val cartItems by viewModel.cartItems.collectAsState()
        var showCheckoutDialog by remember { mutableStateOf(false) }

        Button(
            onClick = { showCheckoutDialog = true },
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Finalizar Compra", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        if (showCheckoutDialog) {
            CheckoutFormDialog(
                viewModel = viewModel,
                onDismiss = { showCheckoutDialog = false },
                onSuccess = { 
                    showCheckoutDialog = false
                    Toast.makeText(context, "¡Compra realizada con éxito!", Toast.LENGTH_LONG).show()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pago seguro procesado por Elara", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DailyCheckInBoard(viewModel: CartViewModel) {
    val claimedDaysCount by viewModel.claimedDaysCount.collectAsState()
    val lastClaimedDate by viewModel.lastClaimedDate.collectAsState()
    val selectedCouponDay by viewModel.selectedCouponDay.collectAsState()
    val isTodayClaimed = viewModel.isTodayClaimed()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bono Diario de Nuevo Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Inicia sesión a diario. ¡Obtén hasta 55% de descuento!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Active coupon indicator
                if (selectedCouponDay > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Activo: -${viewModel.getCouponPercentageForDay(selectedCouponDay)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 10 Days Horizontal Scrolling Row
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (day in 1..10) {
                    val isClaimed = day <= claimedDaysCount
                    val isToday = day == claimedDaysCount + 1 && !isTodayClaimed
                    val isLocked = day > claimedDaysCount + (if (isTodayClaimed) 0 else 1)
                    val isApplied = day == selectedCouponDay
                    val percentage = viewModel.getCouponPercentageForDay(day)

                    // Card for each day
                    Surface(
                        modifier = Modifier
                            .width(72.dp)
                            .clickable(enabled = isClaimed || isToday) {
                                if (isToday) {
                                    viewModel.claimTodayCoupon()
                                } else if (isClaimed) {
                                    viewModel.selectCoupon(day)
                                }
                            },
                        color = when {
                            isApplied -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            isToday -> Color(0xFFFFB300).copy(alpha = 0.15f)
                            isClaimed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        },
                        border = BorderStroke(
                            width = if (isApplied) 2.dp else if (isToday) 2.dp else 1.dp,
                            color = when {
                                isApplied -> MaterialTheme.colorScheme.primary
                                isToday -> Color(0xFFFFB300)
                                isClaimed -> Color.LightGray.copy(alpha = 0.3f)
                                else -> Color.LightGray.copy(alpha = 0.08f)
                            }
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Día $day",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$percentage%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    isToday -> Color(0xFFFFB300)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Status icon or text
                            when {
                                isClaimed -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Reclamado",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                isToday -> {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "¡LISTO!",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "¡LISTO!",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Bloqueado",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action section with countdown timer
            var timeRemaining by remember { mutableStateOf("") }
            LaunchedEffect(claimedDaysCount, lastClaimedDate) {
                while (true) {
                    val now = java.util.Calendar.getInstance()
                    val midnight = java.util.Calendar.getInstance()
                    midnight.add(java.util.Calendar.DAY_OF_YEAR, 1)
                    midnight.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    midnight.set(java.util.Calendar.MINUTE, 0)
                    midnight.set(java.util.Calendar.SECOND, 0)
                    
                    val diffMs = midnight.timeInMillis - now.timeInMillis
                    val hours = diffMs / (1000 * 60 * 60)
                    val minutes = (diffMs % (1000 * 60 * 60)) / (1000 * 60)
                    
                    timeRemaining = String.format("%02dh %02dm", hours, minutes)
                    kotlinx.coroutines.delay(60000)
                }
            }

            if (!isTodayClaimed && claimedDaysCount < 10) {
                Button(
                    onClick = { viewModel.claimTodayCoupon() },
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color(0xFFFFB300)
                                )
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECLAMAR DÍA ${claimedDaysCount + 1} (-${viewModel.getCouponPercentageForDay(claimedDaysCount + 1)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (claimedDaysCount >= 10) "¡Bono completado!" else "Bono de hoy reclamado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (claimedDaysCount < 10) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Siguiente en $timeRemaining",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Demo Tools Row (aligned to right, small and clean)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Simulador de pruebas:",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.clickable { viewModel.simulateNextDay() },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = CircleShape
                ) {
                    Text(
                        text = "+1 Día",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.clickable { viewModel.resetDailyBonus() },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Reset",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isSuccess: Boolean = false, isAI: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isAI) Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
            if (isAI) Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isSuccess) MaterialTheme.colorScheme.secondary else if (isAI) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun CouponSelectionDialog(
    viewModel: CartViewModel,
    onDismiss: () -> Unit
) {
    val claimedDaysCount by viewModel.claimedDaysCount.collectAsState()
    val selectedCouponDay by viewModel.selectedCouponDay.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar Cupón",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Elige uno de tus cupones acumulados por inicio de sesión diario para aplicarlo a tu compra actual:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // None option
                val isNoneSelected = selectedCouponDay == 0
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            viewModel.selectCoupon(0)
                            onDismiss()
                        },
                    color = if (isNoneSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isNoneSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Sin cupón",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Comprar a precio regular",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isNoneSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seleccionado",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // List of claimed coupons
                for (day in 1..claimedDaysCount) {
                    val percentage = viewModel.getCouponPercentageForDay(day)
                    val isSelected = selectedCouponDay == day

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.selectCoupon(day)
                                onDismiss()
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Cupón de Descuento ($percentage%)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Acumulado del Día $day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutFormDialog(
    viewModel: CartViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    val userState by viewModel.currentUserState.collectAsState()

    LaunchedEffect(userState) {
        val uid = userState?.uid
        if (uid != null && !uid.startsWith("offline_")) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val dbAddresses = snapshot.get("shipping_addresses") as? List<Map<String, Any>>
                        val firstAddress = dbAddresses?.firstOrNull()
                        if (firstAddress != null) {
                            val fullAddress = firstAddress["fullAddress"] as? String ?: ""
                            val city = firstAddress["city"] as? String ?: ""
                            val postalCode = firstAddress["postalCode"] as? String ?: ""
                            val formatted = listOf(fullAddress, city, postalCode).filter { it.isNotEmpty() }.joinToString(", ")
                            if (formatted.isNotEmpty() && address.isEmpty()) {
                                address = formatted
                            }
                        }
                        
                        val firstName = snapshot.getString("firstName") ?: ""
                        val lastName = snapshot.getString("lastName") ?: ""
                        val dbName = if (firstName.isNotEmpty() || lastName.isNotEmpty()) "$firstName $lastName".trim() else snapshot.getString("name") ?: ""
                        if (dbName.isNotEmpty() && fullName.isEmpty()) {
                            fullName = dbName
                        }
                        val dbPhone = snapshot.getString("phone") ?: ""
                        if (dbPhone.isNotEmpty() && phone.isEmpty()) {
                            phone = dbPhone
                        }
                    }
                }
        }
    }

    var fullNameError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var cardNumberError by remember { mutableStateOf("") }
    var cardExpiryError by remember { mutableStateOf("") }
    var cardCvvError by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Column {
                Text(
                    text = "Finalizar Compra",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Complete sus datos de envío y pago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { 
                        fullName = it
                        fullNameError = ""
                    },
                    label = { Text("Nombre Completo") },
                    isError = fullNameError.isNotEmpty(),
                    supportingText = { if (fullNameError.isNotEmpty()) Text(fullNameError, color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                // Shipping Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { 
                        address = it
                        addressError = ""
                    },
                    label = { Text("Dirección de Envío") },
                    isError = addressError.isNotEmpty(),
                    supportingText = { if (addressError.isNotEmpty()) Text(addressError, color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                // Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it.filter { char -> char.isDigit() }
                        phoneError = ""
                    },
                    label = { Text("Teléfono de Contacto") },
                    isError = phoneError.isNotEmpty(),
                    supportingText = { if (phoneError.isNotEmpty()) Text(phoneError, color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                // Card Number
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { 
                        val filtered = it.filter { char -> char.isDigit() }
                        if (filtered.length <= 16) {
                            cardNumber = filtered
                            cardNumberError = ""
                        }
                    },
                    label = { Text("Número de Tarjeta") },
                    placeholder = { Text("16 dígitos") },
                    isError = cardNumberError.isNotEmpty(),
                    supportingText = { if (cardNumberError.isNotEmpty()) Text(cardNumberError, color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Expiry
                    OutlinedTextField(
                        value = cardExpiry,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() || char == '/' }
                            if (filtered.length <= 5) {
                                cardExpiry = filtered
                                cardExpiryError = ""
                            }
                        },
                        label = { Text("Expiración (MM/AA)") },
                        placeholder = { Text("12/28") },
                        isError = cardExpiryError.isNotEmpty(),
                        supportingText = { if (cardExpiryError.isNotEmpty()) Text(cardExpiryError, color = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    // CVV
                    OutlinedTextField(
                        value = cardCvv,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.length <= 3) {
                                cardCvv = filtered
                                cardCvvError = ""
                            }
                        },
                        label = { Text("CVV") },
                        placeholder = { Text("3 dígitos") },
                        isError = cardCvvError.isNotEmpty(),
                        supportingText = { if (cardCvvError.isNotEmpty()) Text(cardCvvError, color = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validations
                    var hasError = false

                    if (fullName.trim().split(" ").filter { it.isNotEmpty() }.size < 2) {
                        fullNameError = "Ingrese nombre y apellido completo"
                        hasError = true
                    }
                    if (address.trim().length < 10) {
                        addressError = "Ingrese dirección válida (mínimo 10 caracteres)"
                        hasError = true
                    }
                    if (phone.trim().length != 9) {
                        phoneError = "Ingrese un teléfono válido (9 dígitos)"
                        hasError = true
                    }
                    if (cardNumber.trim().length != 16) {
                        cardNumberError = "Ingrese tarjeta de 16 dígitos"
                        hasError = true
                    }
                    val expiryRegex = Regex("^(0[1-9]|1[0-2])/([0-9]{2})$")
                    if (!cardExpiry.matches(expiryRegex)) {
                        cardExpiryError = "Ingrese expiración válida (MM/AA)"
                        hasError = true
                    }
                    if (cardCvv.trim().length != 3) {
                        cardCvvError = "Ingrese CVV de 3 dígitos"
                        hasError = true
                    }

                    if (!hasError) {
                        isSubmitting = true
                        viewModel.checkout(
                            fullName = fullName,
                            address = address,
                            phone = phone,
                            cardNumber = cardNumber,
                            cardExpiry = cardExpiry,
                            cardCvv = cardCvv
                        ) { result ->
                            isSubmitting = false
                            if (result.isSuccess) {
                                onSuccess()
                            }
                        }
                    }
                },
                enabled = !isSubmitting,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Pagar $${String.format("%.2f", viewModel.totalAmount)}")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

