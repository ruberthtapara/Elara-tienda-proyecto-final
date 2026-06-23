package com.example.proyect_final.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.proyect_final.ui.components.CameraPreview
import com.example.proyect_final.ui.viewmodel.AdvisorState
import com.example.proyect_final.ui.viewmodel.StyleAdvisorViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleAdvisorScreen(
    productId: String,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: StyleAdvisorViewModel = viewModel(factory = StyleAdvisorViewModel.provideFactory(productId))
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val product by viewModel.product.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    DisposableEffect(context) {
        onDispose {
            cameraExecutor.shutdown()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                } catch (e: Exception) {
                    Log.e("Camera", "Error desvinculando cámara al cerrar pantalla", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("ESTILISTA ELARA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Product Context Card
                product?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = com.example.proyect_final.data.remote.getProductImageModel(it.image),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "CONCILIANDO CON",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = capturedBitmap,
                    label = "advisor_content"
                ) { bitmap ->
                    if (bitmap == null) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.75f)
                                    .padding(horizontal = 24.dp)
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .background(Color.Black)
                            ) {
                                if (hasCameraPermission) {
                                    CameraPreview(
                                        modifier = Modifier.fillMaxSize(),
                                        onPreviewCreated = { preview ->
                                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                                            cameraProviderFuture.addListener({
                                                val cameraProvider = cameraProviderFuture.get()
                                                try {
                                                    cameraProvider.unbindAll()
                                                    cameraProvider.bindToLifecycle(
                                                        lifecycleOwner,
                                                        androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                                                        preview,
                                                        imageCapture
                                                    )
                                                } catch (e: Exception) {
                                                    Log.e("Camera", "Fallo al vincular cámara", e)
                                                }
                                            }, ContextCompat.getMainExecutor(context))
                                        }
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 32.dp)
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.3f))
                                            .padding(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .clickable {
                                                imageCapture.takePicture(
                                                    cameraExecutor,
                                                    object : ImageCapture.OnImageCapturedCallback() {
                                                        override fun onCaptureSuccess(image: ImageProxy) {
                                                            val buffer = image.planes[0].buffer
                                                            val bytes = ByteArray(buffer.remaining())
                                                            buffer.get(bytes)
                                                            val decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                            // Escalar la imagen tomada para subirla súper rápido
                                                            val scaledBitmap = com.example.proyect_final.util.ImageUtils.scaleBitmap(decodedBitmap, 768)
                                                            capturedBitmap = scaledBitmap
                                                            image.close()
                                                        }
                                                        override fun onError(exception: ImageCaptureException) {
                                                            Log.e("Camera", "Fallo en captura", exception)
                                                        }
                                                    }
                                                )
                                            }
                                    )
                                }
                            }
                            
                            Text(
                                text = "Escanea tu prenda para recibir consejos de estilo personalizados de nuestra IA Elara.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp)
                                    .clip(MaterialTheme.shapes.extraLarge)
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                 IconButton(
                                     onClick = {
                                         viewModel.resetAdvisorState()
                                         capturedBitmap = null
                                     },
                                     modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                 ) {
                                     Icon(Icons.Default.Refresh, contentDescription = "Repetir", tint = Color.White)
                                 }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            when (val state = uiState) {
                                is AdvisorState.Idle -> {
                                    Button(
                                        onClick = { viewModel.getAdvice(bitmap) },
                                        modifier = Modifier.fillMaxWidth().height(64.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("ANALIZAR CON ELARA", fontWeight = FontWeight.Bold)
                                    }
                                }
                                is AdvisorState.Loading -> {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
                                    Text("Elara está analizando tu outfit...", modifier = Modifier.padding(top = 16.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                                }
                                is AdvisorState.Success -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(24.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("CONSEJO DE ELARA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(state.advice, style = MaterialTheme.typography.bodyLarge)
                                            Spacer(modifier = Modifier.height(24.dp))
                                             Button(
                                                 onClick = {
                                                     viewModel.resetAdvisorState()
                                                     capturedBitmap = null
                                                 },
                                                 modifier = Modifier.fillMaxWidth()
                                             ) {
                                                 Text("ESCANEAR OTRA PRENDA")
                                             }
                                        }
                                    }
                                    if (state.alternativeProducts.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text(
                                            text = "Prendas similares que te pueden gustar:",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(state.alternativeProducts) { altProduct ->
                                                RecommendationCard(product = altProduct, onClick = { onProductClick(altProduct.id) })
                                            }
                                        }
                                    }
                                }
                                is AdvisorState.Error -> {
                                    Text("Error: ${state.message}", color = Color.Red)
                                    Button(onClick = { viewModel.getAdvice(bitmap) }) { Text("Reintentar") }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun RecommendationCard(
    product: com.example.proyect_final.domain.model.Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = product.image,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
