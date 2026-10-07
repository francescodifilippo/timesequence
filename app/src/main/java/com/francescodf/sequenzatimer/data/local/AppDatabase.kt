package com.francescodf.sequenzatimer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.francescodf.sequenzatimer.data.model.TimerSequence

@Database(
    entities = [TimerSequence::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sequenceDao(): SequenceDao
}
