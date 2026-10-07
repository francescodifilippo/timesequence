package com.francescodf.sequenzatimer.data.repository

import com.francescodf.sequenzatimer.data.local.SequenceDao
import com.francescodf.sequenzatimer.data.model.TimerSequence
import kotlinx.coroutines.flow.Flow

class SequenceRepositoryImpl(
    private val dao: SequenceDao
) : SequenceRepository {

    override fun getAllSequences(): Flow<List<TimerSequence>> =
        dao.getAllSequences()

    override suspend fun getSequenceById(id: Long): TimerSequence? =
        dao.getSequenceById(id)

    override suspend fun saveSequence(sequence: TimerSequence): Long =
        dao.upsertSequence(sequence.copy(updatedAt = System.currentTimeMillis()))

    override suspend fun deleteSequence(id: Long) =
        dao.deleteSequenceById(id)
}
