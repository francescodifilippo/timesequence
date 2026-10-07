package com.francescodf.sequenzatimer.domain.timer

import com.francescodf.sequenzatimer.data.model.TimerSequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerEngine {
    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    // Emits when a phase transition occurs (for haptic/sound feedback)
    private val _phaseTransition = MutableSharedFlow<PhaseTransitionEvent>(extraBufferCapacity = 1)
    val phaseTransition: SharedFlow<PhaseTransitionEvent> = _phaseTransition.asSharedFlow()

    private var timerJob: Job? = null
    private val tickIntervalMs = 50L

    @Volatile
    private var skipRequested = false

    private var lastSequence: TimerSequence? = null
    private var lastScope: CoroutineScope? = null

    fun start(sequence: TimerSequence, scope: CoroutineScope) {
        stop()
        lastSequence = sequence
        lastScope = scope
        timerJob = scope.launch {
            runSequence(sequence)
        }
    }

    fun restart() {
        val sequence = lastSequence ?: return
        val scope = lastScope ?: return
        start(sequence, scope)
    }

    private suspend fun runSequence(sequence: TimerSequence) {
        val phases = sequence.phases
        if (phases.isEmpty()) {
            _state.value = TimerState.Finished(0)
            return
        }

        val totalCycles = sequence.repeatCount ?: -1
        val totalDurationLimitMs = sequence.totalDurationSeconds?.let { it * 1000L }
        var overallElapsedMs = 0L
        var cycle = 1

        while (true) {
            if (totalCycles > 0 && cycle > totalCycles) break

            for ((phaseIndex, phase) in phases.withIndex()) {
                val phaseDurationMs = phase.durationSeconds * 1000L
                var elapsedInPhaseMs = 0L
                skipRequested = false

                _state.value = TimerState.Running(
                    currentPhaseIndex = phaseIndex,
                    currentCycle = cycle,
                    totalCycles = totalCycles,
                    elapsedInPhaseMs = 0,
                    phaseDurationMs = phaseDurationMs,
                    phases = phases,
                    isPaused = false
                )

                // Emit phase transition event (skip initial to avoid vibrating on start)
                if (phaseIndex > 0 || cycle > 1) {
                    _phaseTransition.tryEmit(PhaseTransitionEvent.PhaseChanged)
                }

                while (elapsedInPhaseMs < phaseDurationMs) {
                    // Check skip via volatile flag
                    if (skipRequested) {
                        skipRequested = false
                        break
                    }

                    val currentState = _state.value
                    if (currentState is TimerState.Running && currentState.isPaused) {
                        delay(tickIntervalMs)
                        continue
                    }

                    delay(tickIntervalMs)
                    elapsedInPhaseMs += tickIntervalMs
                    overallElapsedMs += tickIntervalMs

                    if (totalDurationLimitMs != null && overallElapsedMs >= totalDurationLimitMs) {
                        _state.value = TimerState.Finished(completedCycles = cycle)
                        _phaseTransition.tryEmit(PhaseTransitionEvent.SequenceFinished)
                        return
                    }

                    _state.value = TimerState.Running(
                        currentPhaseIndex = phaseIndex,
                        currentCycle = cycle,
                        totalCycles = totalCycles,
                        elapsedInPhaseMs = elapsedInPhaseMs,
                        phaseDurationMs = phaseDurationMs,
                        phases = phases,
                        isPaused = false
                    )
                }
            }
            cycle++
        }
        _state.value = TimerState.Finished(completedCycles = cycle - 1)
        _phaseTransition.tryEmit(PhaseTransitionEvent.SequenceFinished)
    }

    fun togglePause() {
        val current = _state.value
        if (current is TimerState.Running) {
            _state.value = current.copy(isPaused = !current.isPaused)
        }
    }

    fun skipPhase() {
        skipRequested = true
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        skipRequested = false
        _state.value = TimerState.Idle
    }
}

sealed interface PhaseTransitionEvent {
    data object PhaseChanged : PhaseTransitionEvent
    data object SequenceFinished : PhaseTransitionEvent
}
