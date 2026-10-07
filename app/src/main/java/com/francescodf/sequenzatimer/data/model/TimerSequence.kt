package com.francescodf.sequenzatimer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_sequences")
data class TimerSequence(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phases: List<TimerPhase>,
    val repeatCount: Int? = null,
    val totalDurationSeconds: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
