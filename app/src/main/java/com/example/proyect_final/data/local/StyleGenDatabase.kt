package com.example.proyect_final.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ClosetEntity::class, StyleAdviceEntity::class], version = 1, exportSchema = false)
abstract class StyleGenDatabase : RoomDatabase() {
    abstract val dao: StyleGenDao

    companion object {
        const val DATABASE_NAME = "stylegen_db"
    }
}
