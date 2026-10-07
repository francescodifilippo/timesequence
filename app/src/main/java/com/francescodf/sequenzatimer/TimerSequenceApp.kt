package com.francescodf.sequenzatimer

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.francescodf.sequenzatimer.data.local.AppDatabase
import com.francescodf.sequenzatimer.data.model.TimerPhase
import com.francescodf.sequenzatimer.data.model.TimerSequence
import com.francescodf.sequenzatimer.data.repository.SequenceRepository
import com.francescodf.sequenzatimer.data.repository.SequenceRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TimerSequenceApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "sequenza_timer.db"
        )
            .addCallback(PrepopulateCallback())
            .build()
    }

    val repository: SequenceRepository by lazy {
        SequenceRepositoryImpl(database.sequenceDao())
    }

    private inner class PrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = database.sequenceDao()

                // Studi Guidotti Respirazione
                dao.upsertSequence(
                    TimerSequence(
                        name = "Studi Guidotti Respirazione",
                        phases = listOf(
                            TimerPhase("Inspira", 10, 0xFF4CAF50L),
                            TimerPhase("Trattieni", 10, 0xFF2196F3L),
                            TimerPhase("Espira", 10, 0xFFF44336L)
                        ),
                        totalDurationSeconds = 300
                    )
                )

                // Esercizi ventre piatto
                dao.upsertSequence(
                    TimerSequence(
                        name = "Esercizi ventre piatto",
                        phases = listOf(
                            TimerPhase("Contrai", 30, 0xFFFF9800L),
                            TimerPhase("Pausa", 5, 0xFF607D8BL)
                        ),
                        repeatCount = 5
                    )
                )
            }
        }
    }
}
