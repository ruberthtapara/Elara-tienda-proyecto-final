package com.example.proyect_final.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StyleGenDao {
    // Closet Items
    @Query("SELECT * FROM closet_items ORDER BY timestamp DESC")
    fun getAllClosetItems(): Flow<List<ClosetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosetItem(item: ClosetEntity)

    @Query("DELETE FROM closet_items WHERE id = :id")
    suspend fun deleteClosetItem(id: Int)

    // Style Advice
    @Query("SELECT * FROM style_advice ORDER BY timestamp DESC")
    fun getAllAdvice(): Flow<List<StyleAdviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvice(advice: StyleAdviceEntity)
}
