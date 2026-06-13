package com.example.proyect_final.domain.repository

import android.graphics.Bitmap
import com.example.proyect_final.data.local.StyleAdviceEntity
import kotlinx.coroutines.flow.Flow

data class StyleAdviceResult(
    val adviceText: String,
    val userImagePath: String
)

interface StyleAdvisorRepository {
    suspend fun getStyleAdvice(
        shopProductImage: String,
        userProductImage: Bitmap,
        isAccessory: Boolean = false
    ): Result<StyleAdviceResult>

    fun getAllAdvice(): Flow<List<StyleAdviceEntity>>
    suspend fun saveAdvice(advice: StyleAdviceEntity)
}
