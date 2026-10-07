package com.francescodf.sequenzatimer.ui.screens.playback

import android.app.Application
import android.media.AudioAttributes
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francescodf.sequenzatimer.data.repository.SequenceRepository
import com.francescodf.sequenzatimer.domain.timer.PhaseTransitionEvent
import com.francescodf.sequenzatimer.domain.timer.TimerEngine
import com.francescodf.sequenzatimer.domain.timer.TimerState
import com.francescodf.sequenzatimer.util.HapticFeedbackHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaybackViewModel(
    application: Application,
    private val repository: SequenceRepository,
    private val sequenceId: Long
) : AndroidViewModel(application) {

    private val timerEngine = TimerEngine()
    private var toneGenerator: ToneGenerator? = null

    val timerState: StateFlow<TimerState> = timerEngine.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimerState.Idle)

    private val _sequenceNotFound = MutableStateFlow(false)
    val sequenceNotFound: StateFlow<Boolean> = _sequenceNotFound.asStateFlow()

    init {
        try {
            toneGenerator = ToneGenerator(
                AudioAttributes.USAGE_NOTIFICATION_EVENT,
                80
            )
        } catch (_: Exception) {
            // ToneGenerator may fail on some devices
        }

        viewModelScope.launch {
            val sequence = repository.getSequenceById(sequenceId)
            if (sequence != null) {
                timerEngine.start(sequence, viewModelScope)
            } else {
                _sequenceNotFound.value = true
            }
        }

        // Listen for phase transition events
        viewModelScope.launch {
            timerEngine.phaseTransition.collect { event ->
                when (event) {
                    PhaseTransitionEvent.PhaseChanged -> {
                        HapticFeedbackHelper.vibratePhaseTransition(getApplication())
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                    }
                    PhaseTransitionEvent.SequenceFinished -> {
                        HapticFeedbackHelper.vibrateSequenceComplete(getApplication())
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
                    }
                }
            }
        }
    }

    fun togglePause() = timerEngine.togglePause()
    fun skipPhase() = timerEngine.skipPhase()
    fun stop() = timerEngine.stop()
    fun restart() = timerEngine.restart()

    override fun onCleared() {
        super.onCleared()
        timerEngine.stop()
        toneGenerator?.release()
        toneGenerator = null
    }
}
