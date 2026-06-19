package com.example.proyect_final.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyect_final.ui.theme.MontserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphicTopBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth(),
        border = null // Will be handled by the layout if needed
    ) {
        Column {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = MontserratFont,
                            letterSpacing = 4.sp,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                thickness = 1.dp
            )
        }
    }
}
