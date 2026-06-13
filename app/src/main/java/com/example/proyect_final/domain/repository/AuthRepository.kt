package com.example.proyect_final.domain.repository

import com.example.proyect_final.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, firstName: String, lastName: String): Result<Unit>
    suspend fun logout()
    suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit>
}
