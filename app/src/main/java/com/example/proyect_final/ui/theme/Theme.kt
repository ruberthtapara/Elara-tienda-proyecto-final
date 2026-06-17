package com.example.proyect_final.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun getCustomColorScheme(palette: String): ColorScheme {
    val primaryColor: Color
    val onPrimaryColor: Color
    val primaryContainerColor: Color
    val onPrimaryContainerColor: Color
    
    val secondaryColor: Color
    val onSecondaryColor: Color
    val secondaryContainerColor: Color
    val onSecondaryContainerColor: Color
    
    val backgroundColor: Color
    val onBackgroundColor: Color
    
    val surfaceColor: Color
    val onSurfaceColor: Color
    
    val surfaceVariantColor: Color
    val onSurfaceVariantColor: Color
    
    val outlineColor: Color
    val outlineVariantColor: Color
    
    when (palette) {
        "Monocromo" -> {
            primaryColor = Color(0xFFFFFFFF)
            onPrimaryColor = Color(0xFF000000)
            primaryContainerColor = Color(0xFF27272A)
            onPrimaryContainerColor = Color(0xFFFAFAFA)
            
            secondaryColor = Color(0xFFE2E8F0)
            onSecondaryColor = Color(0xFF000000)
            secondaryContainerColor = Color(0xFF18181B)
            onSecondaryContainerColor = Color(0xFFE2E8F0)
            
            backgroundColor = Color(0xFF09090B)
            onBackgroundColor = Color(0xFFFAFAFA)
            
            surfaceColor = Color(0xFF18181B)
            onSurfaceColor = Color(0xFFFAFAFA)
            
            surfaceVariantColor = Color(0xFF27272A)
            onSurfaceVariantColor = Color(0xFFD4D4D8)
            
            outlineColor = Color(0xFF3F3F46)
            outlineVariantColor = Color(0xFF27272A)
        }
        "Azul Aurora" -> {
            primaryColor = Color(0xFF38BDF8)
            onPrimaryColor = Color(0xFF0F172A)
            primaryContainerColor = Color(0xFF0C4A6E)
            onPrimaryContainerColor = Color(0xFFE0F2FE)
            
            secondaryColor = Color(0xFF0EA5E9)
            onSecondaryColor = Color(0xFFFFFFFF)
            secondaryContainerColor = Color(0xFF0F172A)
            onSecondaryContainerColor = Color(0xFF38BDF8)
            
            backgroundColor = Color(0xFF0B132B)
            onBackgroundColor = Color(0xFFF1F5F9)
            
            surfaceColor = Color(0xFF1C2541)
            onSurfaceColor = Color(0xFFF1F5F9)
            
            surfaceVariantColor = Color(0xFF3A506B)
            onSurfaceVariantColor = Color(0xFFCBD5E1)
            
            outlineColor = Color(0xFF475569)
            outlineVariantColor = Color(0xFF334155)
        }
        "Oro Imperial" -> {
            primaryColor = Color(0xFFD4AF37)
            onPrimaryColor = Color(0xFF1A1A1A)
            primaryContainerColor = Color(0xFF452A05)
            onPrimaryContainerColor = Color(0xFFFEF9C3)
            
            secondaryColor = Color(0xFFFBBF24)
            onSecondaryColor = Color(0xFF1A1A1A)
            secondaryContainerColor = Color(0xFF1C1917)
            onSecondaryContainerColor = Color(0xFFFBBF24)
            
            backgroundColor = Color(0xFF110F0C)
            onBackgroundColor = Color(0xFFFAFAF9)
            
            surfaceColor = Color(0xFF1C1917)
            onSurfaceColor = Color(0xFFFAFAF9)
            
            surfaceVariantColor = Color(0xFF292524)
            onSurfaceVariantColor = Color(0xFFD6D3D1)
            
            outlineColor = Color(0xFF44403C)
            outlineVariantColor = Color(0xFF292524)
        }
        "Esmeralda" -> {
            primaryColor = Color(0xFF10B981)
            onPrimaryColor = Color(0xFF022C22)
            primaryContainerColor = Color(0xFF064E3B)
            onPrimaryContainerColor = Color(0xFFD1FAE5)
            
            secondaryColor = Color(0xFF34D399)
            onSecondaryColor = Color(0xFF022C22)
            secondaryContainerColor = Color(0xFF121A16)
            onSecondaryContainerColor = Color(0xFF34D399)
            
            backgroundColor = Color(0xFF0A0F0D)
            onBackgroundColor = Color(0xFFF0FDF4)
            
            surfaceColor = Color(0xFF121A16)
            onSurfaceColor = Color(0xFFF0FDF4)
            
            surfaceVariantColor = Color(0xFF1C2E24)
            onSurfaceVariantColor = Color(0xFFA7F3D0)
            
            outlineColor = Color(0xFF2D4A3A)
            outlineVariantColor = Color(0xFF1C2E24)
        }
        else -> { // Fallback/Default to Monocromo
            primaryColor = Color(0xFFFFFFFF)
            onPrimaryColor = Color(0xFF000000)
            primaryContainerColor = Color(0xFF27272A)
            onPrimaryContainerColor = Color(0xFFFAFAFA)
            
            secondaryColor = Color(0xFFE2E8F0)
            onSecondaryColor = Color(0xFF000000)
            secondaryContainerColor = Color(0xFF18181B)
            onSecondaryContainerColor = Color(0xFFE2E8F0)
            
            backgroundColor = Color(0xFF09090B)
            onBackgroundColor = Color(0xFFFAFAFA)
            
            surfaceColor = Color(0xFF18181B)
            onSurfaceColor = Color(0xFFFAFAFA)
            
            surfaceVariantColor = Color(0xFF27272A)
            onSurfaceVariantColor = Color(0xFFD4D4D8)
            
            outlineColor = Color(0xFF3F3F46)
            outlineVariantColor = Color(0xFF27272A)
        }
    }
    
    return lightColorScheme(
        primary = primaryColor,
        onPrimary = onPrimaryColor,
        primaryContainer = primaryContainerColor,
        onPrimaryContainer = onPrimaryContainerColor,
        secondary = secondaryColor,
        onSecondary = onSecondaryColor,
        secondaryContainer = secondaryContainerColor,
        onSecondaryContainer = onSecondaryContainerColor,
        error = ElaraError,
        onError = ElaraWhite,
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFCA5A5),
        background = backgroundColor,
        onBackground = onBackgroundColor,
        surface = surfaceColor,
        onSurface = onSurfaceColor,
        surfaceVariant = surfaceVariantColor,
        onSurfaceVariant = onSurfaceVariantColor,
        outline = outlineColor,
        outlineVariant = outlineVariantColor
    )
}

@Composable
fun Proyect_finalTheme(
    colorPalette: String = "Monocromo",
    content: @Composable () -> Unit
) {
    val colorScheme = getCustomColorScheme(colorPalette)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}