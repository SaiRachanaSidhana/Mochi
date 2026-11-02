package com.mochi.feature.settings

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mochi.app.R
import com.mochi.core.designsystem.MochiTheme
import com.mochi.core.model.NudgeSettings
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    showDebugTools: Boolean,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        showDebugTools = showDebugTools,
        onBack = onBack,
        onDoomscrollLimitSelected = viewModel::onDoomscrollLimitSelected,
        onBedtimeChanged = viewModel::onBedtimeChanged,
        onWakeTimeChanged = viewModel::onWakeTimeChanged,
        onCooldownSelected = viewModel::onCooldownSelected,
        onLockEnabledChanged = viewModel::onLockEnabledChanged,
        onResetToDefaults = viewModel::onResetToDefaults,
        onDebugDoomscrollPreset = viewModel::onDebugDoomscrollPreset,
        onDebugBedtimePreset = viewModel::onDebugBedtimePreset,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    showDebugTools: Boolean,
    onBack: () -> Unit,
    onDoomscrollLimitSelected: (Int) -> Unit,
    onBedtimeChanged: (LocalTime) -> Unit,
    onWakeTimeChanged: (LocalTime) -> Unit,
    onCooldownSelected: (Int) -> Unit,
    onLockEnabledChanged: (Boolean) -> Unit,
    onResetToDefaults: () -> Unit,
    onDebugDoomscrollPreset: () -> Unit,
    onDebugBedtimePreset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val settings = state.settings

            SettingsCard(
                title = stringResource(R.string.settings_doomscroll_title),
                description = stringResource(R.string.settings_doomscroll_description),
            ) {
                MinuteOptions(
                    options = NudgeSettings.DOOMSCROLL_LIMIT_OPTIONS_MINUTES,
                    selectedMinutes = settings.doomscrollLimit.toMinutes().toInt(),
                    onSelected = onDoomscrollLimitSelected,
                )
            }

            SettingsCard(
                title = stringResource(R.string.settings_bedtime_title),
                description = stringResource(R.string.settings_bedtime_description),
            ) {
                TimeRow(
                    label = stringResource(R.string.settings_bedtime_title),
                    time = settings.bedtime,
                    onTimeChanged = onBedtimeChanged,
                )
                TimeRow(
                    label = stringResource(R.string.settings_wake_title),
                    time = settings.wakeTime,
                    onTimeChanged = onWakeTimeChanged,
                )
            }

            SettingsCard(
                title = stringResource(R.string.settings_cooldown_title),
                description = stringResource(R.string.settings_cooldown_description),
            ) {
                MinuteOptions(
                    options = NudgeSettings.COOLDOWN_OPTIONS_MINUTES,
                    selectedMinutes = settings.cooldown.toMinutes().toInt(),
                    onSelected = onCooldownSelected,
                )
            }

            SettingsCard(
                title = stringResource(R.string.settings_lock_title),
                description = stringResource(R.string.settings_lock_description),
            ) {
                val lockLabel = stringResource(R.string.settings_lock_title)
                Switch(
                    checked = settings.lockEnabled,
                    onCheckedChange = onLockEnabledChanged,
                    modifier =
                        Modifier
                            .heightIn(min = 48.dp)
                            .semantics { contentDescription = lockLabel },
                )
            }

            OutlinedButton(
                onClick = onResetToDefaults,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.settings_reset), style = MaterialTheme.typography.labelLarge)
            }

            if (showDebugTools) {
                DebugCard(onDoomscrollPreset = onDebugDoomscrollPreset, onBedtimePreset = onDebugBedtimePreset)
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MinuteOptions(
    options: List<Int>,
    selectedMinutes: Int,
    onSelected: (Int) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { minutes ->
            FilterChip(
                selected = minutes == selectedMinutes,
                onClick = { onSelected(minutes) },
                label = { Text(stringResource(R.string.settings_minutes_option, minutes)) },
            )
        }
    }
}

@Composable
private fun TimeRow(
    label: String,
    time: LocalTime,
    onTimeChanged: (LocalTime) -> Unit,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val formatted = rememberTimeFormatter().format(time)
    val description = stringResource(R.string.settings_time_row_description, label, formatted)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button) { showPicker = true }
                .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(formatted, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }

    if (showPicker) {
        TimePickerDialog(
            initial = time,
            onConfirm = {
                onTimeChanged(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun rememberTimeFormatter(): DateTimeFormatter {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    return remember(is24Hour) { DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a") }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState =
        rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = DateFormat.is24HourFormat(LocalContext.current),
        )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(R.string.settings_time_picker_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_time_picker_cancel)) }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = pickerState)
            }
        },
    )
}

@Composable
private fun DebugCard(
    onDoomscrollPreset: () -> Unit,
    onBedtimePreset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.settings_debug_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_debug_description), style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onDoomscrollPreset, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.settings_debug_doomscroll))
            }
            OutlinedButton(onClick = onBedtimePreset, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.settings_debug_bedtime))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MochiTheme {
        SettingsScreen(
            state = SettingsUiState(),
            showDebugTools = true,
            onBack = {},
            onDoomscrollLimitSelected = {},
            onBedtimeChanged = {},
            onWakeTimeChanged = {},
            onCooldownSelected = {},
            onLockEnabledChanged = {},
            onResetToDefaults = {},
            onDebugDoomscrollPreset = {},
            onDebugBedtimePreset = {},
        )
    }
}
