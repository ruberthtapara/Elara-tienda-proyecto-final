package com.example.proyect_final.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "style_advice")
data class StyleAdviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shopProductId: String,
    val shopProductImage: String,
    val userProductImage: String,
    val adviceText: String,
    val isGoodMatch: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
