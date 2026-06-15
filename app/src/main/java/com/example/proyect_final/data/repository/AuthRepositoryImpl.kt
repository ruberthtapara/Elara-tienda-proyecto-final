package com.example.proyect_final.data.repository

import android.content.Context
import android.util.Log
import com.example.proyect_final.domain.model.User
import com.example.proyect_final.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val context: Context
) : AuthRepository {

    private val _offlineUser = MutableStateFlow<User?>(null)

    override val currentUser: Flow<User?> = callbackFlow {
        Log.d("AuthRepository", "Iniciando listener de estado de sesión")
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            Log.d("AuthRepository", "Sesión Firebase detectada: ${firebaseUser?.email ?: "Ninguna"}")
            if (firebaseUser != null) {
                trySend(User(firebaseUser.uid, firebaseUser.email))
            } else {
                trySend(_offlineUser.value)
            }
        }
        auth.addAuthStateListener(listener)
        
        val job = CoroutineScope(Dispatchers.Default).launch {
            _offlineUser.collect { user ->
                if (auth.currentUser == null) {
                    trySend(user)
                }
            }
        }
        
        awaitClose { 
            Log.d("AuthRepository", "Removiendo listener de estado de sesión")
            auth.removeAuthStateListener(listener) 
            job.cancel()
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            Log.d("AuthRepository", "Intentando login Firebase para: $email")
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "Login Firebase exitoso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("AuthRepository", "Fallo en Firebase, intentando local offline", e)
            val prefs = context.getSharedPreferences("offline_auth", Context.MODE_PRIVATE)
            val savedPassword = prefs.getString("pwd_$email", null)
            if (savedPassword != null) {
                if (savedPassword == password) {
                    Log.d("AuthRepository", "Login local exitoso")
                    _offlineUser.value = User(uid = "offline_$email", email = email)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Contraseña incorrecta (local)"))
                }
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun register(email: String, password: String, firstName: String, lastName: String): Result<Unit> {
        return try {
            Log.d("AuthRepository", "Intentando registro Firebase para: $email")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                val userMap = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "email" to email,
                    "name" to "$firstName $lastName"
                )
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .set(userMap).await()
            }
            Log.d("AuthRepository", "Registro Firebase y Firestore exitoso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Fallo en registro Firebase", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        Log.d("AuthRepository", "Cerrando sesión")
        auth.signOut()
        _offlineUser.value = null
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val email = firebaseUser.email ?: return Result.failure(Exception("Usuario no tiene correo electrónico asociado"))
                Log.d("AuthRepository", "Reautenticando usuario para cambio de contraseña")
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPassword)
                firebaseUser.reauthenticate(credential).await()
                Log.d("AuthRepository", "Reautenticación exitosa, actualizando contraseña en Firebase Auth")
                firebaseUser.updatePassword(newPassword).await()
            } else {
                val localUser = _offlineUser.value
                if (localUser != null && localUser.email != null) {
                    val prefs = context.getSharedPreferences("offline_auth", Context.MODE_PRIVATE)
                    val email = localUser.email
                    val savedPassword = prefs.getString("pwd_$email", null)
                    if (savedPassword != oldPassword) {
                        return Result.failure(Exception("La contraseña anterior es incorrecta"))
                    }
                    prefs.edit().putString("pwd_$email", newPassword).apply()
                    Log.d("AuthRepository", "Contraseña local actualizada con éxito")
                } else {
                    return Result.failure(Exception("No hay usuario iniciado"))
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al actualizar contraseña", e)
            Result.failure(e)
        }
    }
}
