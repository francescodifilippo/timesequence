package com.francescodf.sequenzatimer.ui.screens.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.francescodf.sequenzatimer.R
import com.francescodf.sequenzatimer.data.repository.SequenceRepository
import com.francescodf.sequenzatimer.ui.components.PhaseColorPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    repository: SequenceRepository,
    sequenceId: Long?,
    onNavigateBack: () -> Unit,
    onSaveAndPlay: (Long) -> Unit,
) {
    val defaultPhaseName = stringResource(R.string.editor_default_phase_name)
    val viewModel: EditorViewModel = viewModel {
        EditorViewModel(repository, sequenceId, defaultPhaseName)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var colorPickerPhaseIndex by remember { mutableIntStateOf(-1) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Intercept back when there are unsaved changes
    BackHandler(enabled = uiState.hasUnsavedChanges) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.editor_discard_title)) },
            text = { Text(stringResource(R.string.editor_discard_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.editor_discard_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.editor_discard_no))
                }
            }
        )
    }

    if (colorPickerPhaseIndex >= 0 && colorPickerPhaseIndex < uiState.phases.size) {
        PhaseColorPicker(
            selectedColorHex = uiState.phases[colorPickerPhaseIndex].colorHex,
            onColorSelected = { color ->
                viewModel.updatePhaseColor(colorPickerPhaseIndex, color)
                colorPickerPhaseIndex = -1
            },
            onDismiss = { colorPickerPhaseIndex = -1 }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) stringResource(R.string.editor_title_edit)
                        else stringResource(R.string.editor_title_new)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.hasUnsavedChanges) showDiscardDialog = true
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sequence name
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text(stringResource(R.string.editor_name_label)) },
                    placeholder = { Text(stringResource(R.string.editor_name_placeholder)) },
                    isError = uiState.nameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Phases header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.editor_phases_header),
                        style = MaterialTheme.typography.titleLarge
                    )
                    OutlinedButton(onClick = viewModel::addPhase) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.editor_add_phase))
                    }
                }
            }

            // Phase items
            itemsIndexed(uiState.phases) { index, phase ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Color dot
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(phase.colorHex.toULong()))
                                    .clickable { colorPickerPhaseIndex = index }
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            // Phase name
                            OutlinedTextField(
                                value = phase.name,
                                onValueChange = { viewModel.updatePhaseName(index, it) },
                                label = { Text(stringResource(R.string.editor_phase_name_label)) },
                                placeholder = { Text(stringResource(R.string.editor_phase_name_placeholder)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() }),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Duration
                            OutlinedTextField(
                                value = phase.durationSeconds.toString(),
                                onValueChange = { value ->
                                    val seconds = value.filter { it.isDigit() }.toIntOrNull() ?: 1
                                    viewModel.updatePhaseDuration(index, seconds)
                                },
                                label = { Text(stringResource(R.string.editor_phase_duration)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Move up/down
                            IconButton(
                                onClick = { viewModel.movePhaseUp(index) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.cd_move_up))
                            }
                            IconButton(
                                onClick = { viewModel.movePhaseDown(index) },
                                enabled = index < uiState.phases.size - 1
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.cd_move_down))
                            }

                            // Delete
                            if (uiState.phases.size > 1) {
                                IconButton(onClick = { viewModel.removePhase(index) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.cd_remove_phase),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Repeat mode
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.editor_repeat_header),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.repeatMode == RepeatMode.REPEAT_COUNT,
                        onClick = { viewModel.updateRepeatMode(RepeatMode.REPEAT_COUNT) },
                        label = { Text(stringResource(R.string.editor_repeat_count)) }
                    )
                    FilterChip(
                        selected = uiState.repeatMode == RepeatMode.TOTAL_DURATION,
                        onClick = { viewModel.updateRepeatMode(RepeatMode.TOTAL_DURATION) },
                        label = { Text(stringResource(R.string.editor_total_duration)) }
                    )
                    FilterChip(
                        selected = uiState.repeatMode == RepeatMode.INFINITE,
                        onClick = { viewModel.updateRepeatMode(RepeatMode.INFINITE) },
                        label = { Text(stringResource(R.string.editor_infinite)) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                when (uiState.repeatMode) {
                    RepeatMode.REPEAT_COUNT -> {
                        OutlinedTextField(
                            value = uiState.repeatCount.toString(),
                            onValueChange = { value ->
                                val count = value.filter { it.isDigit() }.toIntOrNull() ?: 1
                                viewModel.updateRepeatCount(count)
                            },
                            label = { Text(stringResource(R.string.editor_repeat_count)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                    RepeatMode.TOTAL_DURATION -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = uiState.totalDurationMinutes.toString(),
                                onValueChange = { value ->
                                    val min = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                                    viewModel.updateTotalDurationMinutes(min)
                                },
                                label = { Text(stringResource(R.string.editor_minutes)) },
                                isError = uiState.durationError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(90.dp)
                            )
                            OutlinedTextField(
                                value = uiState.totalDurationSeconds.toString(),
                                onValueChange = { value ->
                                    val sec = value.filter { it.isDigit() }.toIntOrNull() ?: 0
                                    viewModel.updateTotalDurationSeconds(sec)
                                },
                                label = { Text(stringResource(R.string.editor_seconds)) },
                                isError = uiState.durationError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(90.dp)
                            )
                        }
                        if (uiState.durationError) {
                            Text(
                                text = stringResource(R.string.editor_duration_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    RepeatMode.INFINITE -> {
                        Text(
                            text = stringResource(R.string.editor_infinite_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action buttons
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.save { onNavigateBack() } },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        Text(stringResource(R.string.editor_save))
                    }
                    Button(
                        onClick = { viewModel.save(onSaveAndPlay) },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        Text(stringResource(R.string.editor_save_and_play))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
