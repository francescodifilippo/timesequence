package com.francescodf.sequenzatimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.francescodf.sequenzatimer.ui.navigation.AppNavigation
import com.francescodf.sequenzatimer.ui.theme.SequenzaTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TimerSequenceApp
        setContent {
            SequenzaTimerTheme {
                AppNavigation(repository = app.repository)
            }
        }
    }
}
