package com.francescodf.sequenzatimer.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francescodf.sequenzatimer.data.model.TimerSequence
import com.francescodf.sequenzatimer.data.repository.SequenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val sequences: List<TimerSequence> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: SequenceRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.getAllSequences()
        .map { HomeUiState(sequences = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun deleteSequence(id: Long) {
        viewModelScope.launch {
            repository.deleteSequence(id)
        }
    }
}
