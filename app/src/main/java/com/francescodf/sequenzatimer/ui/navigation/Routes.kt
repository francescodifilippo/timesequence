package com.francescodf.sequenzatimer.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data class Editor(val sequenceId: Long = -1L) : Route

    @Serializable
    data class Playback(val sequenceId: Long) : Route
}
