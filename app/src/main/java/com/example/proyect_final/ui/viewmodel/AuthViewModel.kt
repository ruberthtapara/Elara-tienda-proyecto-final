package com.example.proyect_final.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_final.StyleGenApplication
import com.example.proyect_final.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    val currentUser = authRepository.currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess { _uiState.value = AuthState.Authenticated }
                .onFailure { _uiState.value = AuthState.Error(it.message ?: "Error al iniciar sesión") }
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _uiState.value = AuthState.Loading
            authRepository.register(email, password, firstName, lastName)
                .onSuccess { _uiState.value = AuthState.Authenticated }
                .onFailure { _uiState.value = AuthState.Error(it.message ?: "Error al registrarse") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthState.Idle
        }
    }

    fun clearError() {
        _uiState.value = AuthState.Idle
    }

    fun updatePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.updatePassword(oldPassword, newPassword)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Error al cambiar la contraseña") }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StyleGenApplication)
                AuthViewModel(application.container.authRepository)
            }
        }
    }
}
