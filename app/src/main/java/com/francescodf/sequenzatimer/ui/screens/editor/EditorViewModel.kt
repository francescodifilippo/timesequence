package com.francescodf.sequenzatimer.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francescodf.sequenzatimer.data.model.TimerPhase
import com.francescodf.sequenzatimer.data.model.TimerSequence
import com.francescodf.sequenzatimer.data.repository.SequenceRepository
import com.francescodf.sequenzatimer.ui.theme.PhaseColorPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RepeatMode { REPEAT_COUNT, TOTAL_DURATION, INFINITE }

data class EditorUiState(
    val name: String = "",
    val phases: List<TimerPhase> = listOf(
        TimerPhase("Inspira", 10, PhaseColorPalette[0].value.toLong()),
        TimerPhase("Trattieni", 5, PhaseColorPalette[1].value.toLong()),
        TimerPhase("Espira", 10, PhaseColorPalette[2].value.toLong())
    ),
    val repeatMode: RepeatMode = RepeatMode.REPEAT_COUNT,
    val repeatCount: Int = 3,
    val totalDurationMinutes: Int = 5,
    val totalDurationSeconds: Int = 0,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val savedId: Long? = null,
    val nameError: Boolean = false,
    val durationError: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

class EditorViewModel(
    private val repository: SequenceRepository,
    private val sequenceId: Long?,
    private val defaultPhaseName: String = "Phase"
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var existingSequence: TimerSequence? = null

    init {
        if (sequenceId != null) {
            _uiState.update { it.copy(isLoading = true, isEditMode = true) }
            viewModelScope.launch {
                val sequence = repository.getSequenceById(sequenceId)
                if (sequence != null) {
                    existingSequence = sequence
                    _uiState.update {
                        val mode = when {
                            sequence.totalDurationSeconds != null -> RepeatMode.TOTAL_DURATION
                            sequence.repeatCount != null -> RepeatMode.REPEAT_COUNT
                            else -> RepeatMode.INFINITE
                        }
                        it.copy(
                            name = sequence.name,
                            phases = sequence.phases,
                            repeatMode = mode,
                            repeatCount = sequence.repeatCount ?: 3,
                            totalDurationMinutes = (sequence.totalDurationSeconds ?: 300) / 60,
                            totalDurationSeconds = (sequence.totalDurationSeconds ?: 300) % 60,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = false, hasUnsavedChanges = true) }
    }

    fun addPhase() {
        _uiState.update { state ->
            val nextColorIndex = state.phases.size % PhaseColorPalette.size
            val newPhase = TimerPhase(
                name = "",
                durationSeconds = 10,
                colorHex = PhaseColorPalette[nextColorIndex].value.toLong()
            )
            state.copy(phases = state.phases + newPhase, hasUnsavedChanges = true)
        }
    }

    fun removePhase(index: Int) {
        _uiState.update { state ->
            if (state.phases.size > 1) {
                state.copy(phases = state.phases.toMutableList().apply { removeAt(index) }, hasUnsavedChanges = true)
            } else state
        }
    }

    fun updatePhaseName(index: Int, name: String) {
        _uiState.update { state ->
            state.copy(phases = state.phases.toMutableList().apply {
                this[index] = this[index].copy(name = name)
            }, hasUnsavedChanges = true)
        }
    }

    fun updatePhaseDuration(index: Int, seconds: Int) {
        _uiState.update { state ->
            state.copy(phases = state.phases.toMutableList().apply {
                this[index] = this[index].copy(durationSeconds = seconds.coerceAtLeast(1))
            }, hasUnsavedChanges = true)
        }
    }

    fun updatePhaseColor(index: Int, colorHex: Long) {
        _uiState.update { state ->
            state.copy(phases = state.phases.toMutableList().apply {
                this[index] = this[index].copy(colorHex = colorHex)
            }, hasUnsavedChanges = true)
        }
    }

    fun updateRepeatMode(mode: RepeatMode) {
        _uiState.update { it.copy(repeatMode = mode, hasUnsavedChanges = true) }
    }

    fun updateRepeatCount(count: Int) {
        _uiState.update { it.copy(repeatCount = count.coerceIn(1, 99), hasUnsavedChanges = true) }
    }

    fun updateTotalDurationMinutes(minutes: Int) {
        _uiState.update { it.copy(totalDurationMinutes = minutes.coerceIn(0, 59), hasUnsavedChanges = true, durationError = false) }
    }

    fun updateTotalDurationSeconds(seconds: Int) {
        _uiState.update { it.copy(totalDurationSeconds = seconds.coerceIn(0, 59), hasUnsavedChanges = true, durationError = false) }
    }

    fun movePhaseUp(index: Int) {
        if (index <= 0) return
        _uiState.update { state ->
            val list = state.phases.toMutableList()
            val temp = list[index]
            list[index] = list[index - 1]
            list[index - 1] = temp
            state.copy(phases = list, hasUnsavedChanges = true)
        }
    }

    fun movePhaseDown(index: Int) {
        _uiState.update { state ->
            if (index >= state.phases.size - 1) return@update state
            val list = state.phases.toMutableList()
            val temp = list[index]
            list[index] = list[index + 1]
            list[index + 1] = temp
            state.copy(phases = list, hasUnsavedChanges = true)
        }
    }

    fun save(onSaved: (Long) -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }
        if (state.repeatMode == RepeatMode.TOTAL_DURATION &&
            state.totalDurationMinutes * 60 + state.totalDurationSeconds <= 0
        ) {
            _uiState.update { it.copy(durationError = true) }
            return
        }

        _uiState.update { it.copy(isSaving = true, durationError = false) }

        val sequence = TimerSequence(
            id = existingSequence?.id ?: 0,
            name = state.name.trim(),
            phases = state.phases.map { phase ->
                phase.copy(name = phase.name.ifBlank { defaultPhaseName })
            },
            repeatCount = when (state.repeatMode) {
                RepeatMode.REPEAT_COUNT -> state.repeatCount
                else -> null
            },
            totalDurationSeconds = when (state.repeatMode) {
                RepeatMode.TOTAL_DURATION -> state.totalDurationMinutes * 60 + state.totalDurationSeconds
                else -> null
            },
            createdAt = existingSequence?.createdAt ?: System.currentTimeMillis()
        )

        viewModelScope.launch {
            val id = repository.saveSequence(sequence)
            _uiState.update { it.copy(isSaving = false, savedId = id, hasUnsavedChanges = false) }
            onSaved(id)
        }
    }
}
