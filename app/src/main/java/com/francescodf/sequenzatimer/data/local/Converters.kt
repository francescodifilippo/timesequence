package com.francescodf.sequenzatimer.data.local

import androidx.room.TypeConverter
import com.francescodf.sequenzatimer.data.model.TimerPhase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromPhaseList(phases: List<TimerPhase>): String =
        json.encodeToString(phases)

    @TypeConverter
    fun toPhaseList(value: String): List<TimerPhase> =
        try {
            json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
}
