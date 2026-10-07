package com.francescodf.sequenzatimer.data.repository

import com.francescodf.sequenzatimer.data.model.TimerSequence
import kotlinx.coroutines.flow.Flow

interface SequenceRepository {
    fun getAllSequences(): Flow<List<TimerSequence>>
    suspend fun getSequenceById(id: Long): TimerSequence?
    suspend fun saveSequence(sequence: TimerSequence): Long
    suspend fun deleteSequence(id: Long)
}
