package com.francescodf.sequenzatimer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francescodf.sequenzatimer.data.model.TimerSequence
import kotlinx.coroutines.flow.Flow

@Dao
interface SequenceDao {
    @Query("SELECT * FROM timer_sequences ORDER BY updatedAt DESC")
    fun getAllSequences(): Flow<List<TimerSequence>>

    @Query("SELECT * FROM timer_sequences WHERE id = :id")
    suspend fun getSequenceById(id: Long): TimerSequence?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSequence(sequence: TimerSequence): Long

    @Query("DELETE FROM timer_sequences WHERE id = :id")
    suspend fun deleteSequenceById(id: Long)
}
