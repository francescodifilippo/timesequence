package com.francescodf.sequenzatimer.domain.timer

import com.francescodf.sequenzatimer.data.model.TimerPhase

sealed interface TimerState {
    data object Idle : TimerState

    data class Running(
        val currentPhaseIndex: Int,
        val currentCycle: Int,
        val totalCycles: Int,
        val elapsedInPhaseMs: Long,
        val phaseDurationMs: Long,
        val phases: List<TimerPhase>,
        val isPaused: Boolean
    ) : TimerState {
        val currentPhase: TimerPhase get() = phases[currentPhaseIndex]
        val remainingMs: Long get() = (phaseDurationMs - elapsedInPhaseMs).coerceAtLeast(0)
        val remainingSeconds: Int get() = ((remainingMs + 999) / 1000).toInt()
        val progress: Float get() = (remainingMs.toFloat() / phaseDurationMs).coerceIn(0f, 1f)
    }

    data class Finished(
        val completedCycles: Int
    ) : TimerState
}
