package com.francescodf.sequenzatimer.ui.screens.playback

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.francescodf.sequenzatimer.R
import com.francescodf.sequenzatimer.data.repository.SequenceRepository
import com.francescodf.sequenzatimer.domain.timer.TimerState
import com.francescodf.sequenzatimer.ui.components.CountdownRing

@Composable
fun PlaybackScreen(
    repository: SequenceRepository,
    sequenceId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PlaybackViewModel = viewModel {
        PlaybackViewModel(
            context.applicationContext as Application,
            repository,
            sequenceId
        )
    }
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val sequenceNotFound by viewModel.sequenceNotFound.collectAsStateWithLifecycle()
    var showStopConfirm by remember { mutableStateOf(false) }

    // Intercept back button during playback
    BackHandler(enabled = timerState is TimerState.Running) {
        showStopConfirm = true
    }

    if (showStopConfirm) {
        AlertDialog(
            onDismissRequest = { showStopConfirm = false },
            title = { Text(stringResource(R.string.playback_stop_confirm_title)) },
            text = { Text(stringResource(R.string.playback_stop_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showStopConfirm = false
                    viewModel.stop()
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.playback_stop))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirm = false }) {
                    Text(stringResource(R.string.playback_resume))
                }
            }
        )
    }

    // Keep screen on
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (timerState) {
            is TimerState.Running -> {
                val running = timerState as TimerState.Running
                Color(running.currentPhase.colorHex.toULong()).copy(alpha = 0.85f)
            }
            is TimerState.Finished -> Color(0xFF2E7D32)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 500),
        label = "bg_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when (val state = timerState) {
            is TimerState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (sequenceNotFound) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.playback_not_found),
                                color = Color.White,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onNavigateBack) {
                                Text(stringResource(R.string.playback_close))
                            }
                        }
                    } else {
                        Text(
                            stringResource(R.string.playback_loading),
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            is TimerState.Running -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: cycle indicator
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = if (state.totalCycles > 0) {
                            stringResource(R.string.playback_cycle, state.currentCycle, state.totalCycles)
                        } else {
                            stringResource(R.string.playback_cycle_unlimited, state.currentCycle)
                        },
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.titleLarge
                    )

                    // Center: countdown ring
                    CountdownRing(
                        progress = state.progress,
                        remainingSeconds = state.remainingSeconds,
                        phaseName = state.currentPhase.name,
                        phaseColor = Color(state.currentPhase.colorHex.toULong()),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )

                    // Bottom: controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 48.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stop
                        IconButton(
                            onClick = { showStopConfirm = true },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.playback_stop),
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Pause/Resume
                        FloatingActionButton(
                            onClick = { viewModel.togglePause() },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (state.isPaused)
                                    stringResource(R.string.playback_resume)
                                else stringResource(R.string.playback_pause),
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Skip
                        IconButton(
                            onClick = { viewModel.skipPhase() },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = stringResource(R.string.playback_skip),
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            is TimerState.Finished -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.playback_finished),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.restart()
                            },
                            modifier = Modifier.height(48.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                Icons.Default.Replay,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.playback_restart),
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                stringResource(R.string.playback_close),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
