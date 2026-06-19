package com.example.proyect_final.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.extraLarge
            ),
        color = Color.White.copy(alpha = 0.8f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
    }
}
