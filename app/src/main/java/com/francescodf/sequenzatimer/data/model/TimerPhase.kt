package com.francescodf.sequenzatimer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TimerPhase(
    val name: String,
    val durationSeconds: Int,
    val colorHex: Long
)
