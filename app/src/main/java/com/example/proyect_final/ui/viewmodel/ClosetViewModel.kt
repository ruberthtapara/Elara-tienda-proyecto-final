package com.example.proyect_final.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyect_final.StyleGenApplication
import com.example.proyect_final.data.local.StyleAdviceEntity
import com.example.proyect_final.domain.repository.StyleAdvisorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class
ClosetViewModel(
    private val advisorRepository: StyleAdvisorRepository
) : ViewModel() {

    val adviceHistory: StateFlow<List<StyleAdviceEntity>> = advisorRepository.getAllAdvice()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as StyleGenApplication)
                ClosetViewModel(advisorRepository = application.container.styleAdvisorRepository)
            }
        }
    }
}
