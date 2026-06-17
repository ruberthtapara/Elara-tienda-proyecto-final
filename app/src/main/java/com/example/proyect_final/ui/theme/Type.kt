package com.example.proyect_final.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.proyect_final.R

val CormorantFont = FontFamily(
    Font(resId = R.font.cormorant_garamond_light, weight = FontWeight.Light),
    Font(resId = R.font.cormorant_garamond_regular, weight = FontWeight.Normal),
    Font(resId = R.font.cormorant_garamond_medium, weight = FontWeight.Medium),
    Font(resId = R.font.cormorant_garamond_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.cormorant_garamond_bold, weight = FontWeight.Bold)
)

val MontserratFont = FontFamily(
    Font(resId = R.font.montserrat_light, weight = FontWeight.Light),
    Font(resId = R.font.montserrat_regular, weight = FontWeight.Normal),
    Font(resId = R.font.montserrat_medium, weight = FontWeight.Medium),
    Font(resId = R.font.montserrat_semibold, weight = FontWeight.SemiBold),
    Font(resId = R.font.montserrat_bold, weight = FontWeight.Bold)
)

// Set of Material typography styles for a high-fashion editorial look
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CormorantFont,
        fontWeight = FontWeight.Light,
        fontSize = 42.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = CormorantFont,
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.2).sp
    ),
    displaySmall = TextStyle(
        fontFamily = CormorantFont,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = CormorantFont,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CormorantFont,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = CormorantFont,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CormorantFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.Light,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MontserratFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp
    )
)