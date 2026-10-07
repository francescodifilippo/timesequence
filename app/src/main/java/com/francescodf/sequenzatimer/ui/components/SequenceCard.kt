package com.francescodf.sequenzatimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.francescodf.sequenzatimer.R
import com.francescodf.sequenzatimer.data.model.TimerSequence

@Composable
fun SequenceCard(
    sequence: TimerSequence,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = buildSummary(sequence)

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sequence.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(onClick = onPlay) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.cd_play),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Phase color dots
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sequence.phases.forEach { phase ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(phase.colorHex.toULong()))
                    )
                }
            }
        }
    }
}

@Composable
private fun buildSummary(sequence: TimerSequence): String {
    val phaseCount = sequence.phases.size
    val phasesText = if (phaseCount == 1) stringResource(R.string.summary_phase_singular)
        else stringResource(R.string.summary_phase_plural, phaseCount)

    return when {
        sequence.repeatCount != null -> {
            val r = sequence.repeatCount
            val repText = if (r == 1) stringResource(R.string.summary_repetition_singular)
                else stringResource(R.string.summary_repetition_plural, r)
            "$phasesText \u2022 $repText"
        }
        sequence.totalDurationSeconds != null -> {
            val min = sequence.totalDurationSeconds / 60
            val sec = sequence.totalDurationSeconds % 60
            val duration = if (min > 0) "${min}m ${sec}s" else "${sec}s"
            "$phasesText \u2022 ${stringResource(R.string.summary_total_format, duration)}"
        }
        else -> "$phasesText \u2022 ${stringResource(R.string.summary_infinite)}"
    }
}
