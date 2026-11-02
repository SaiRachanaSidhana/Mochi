package com.mochi.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochi.core.data.settings.SettingsRepository
import com.mochi.core.model.NudgeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val clock: Clock,
    ) : ViewModel() {
        val uiState: StateFlow<SettingsUiState> =
            settingsRepository.settings
                .map { SettingsUiState(settings = it) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SettingsUiState())

        fun onDoomscrollLimitSelected(minutes: Int) =
            update { it.copy(doomscrollLimit = Duration.ofMinutes(minutes.toLong())) }

        fun onCooldownSelected(minutes: Int) = update { it.copy(cooldown = Duration.ofMinutes(minutes.toLong())) }

        fun onLockEnabledChanged(enabled: Boolean) = update { it.copy(lockEnabled = enabled) }

        fun onBedtimeChanged(time: LocalTime) = update { it.copy(bedtime = time) }

        fun onWakeTimeChanged(time: LocalTime) = update { it.copy(wakeTime = time) }

        fun onResetToDefaults() {
            viewModelScope.launch { settingsRepository.resetToDefaults() }
        }

        fun onDebugDoomscrollPreset() {
            val farAwayBedtime = now().plusHours(DEBUG_BEDTIME_OFFSET_HOURS)
            update {
                it.copy(
                    doomscrollLimit = Duration.ofMinutes(1),
                    cooldown = Duration.ofMinutes(1),
                    bedtime = farAwayBedtime,
                    wakeTime = farAwayBedtime.plusHours(2),
                )
            }
        }

        fun onDebugBedtimePreset() {
            val bedtime = now()
            update {
                it.copy(
                    cooldown = Duration.ofMinutes(1),
                    bedtime = bedtime,
                    wakeTime = bedtime.plusHours(2),
                )
            }
        }

        private fun now(): LocalTime = LocalTime.now(clock).truncatedTo(ChronoUnit.MINUTES)

        private fun update(transform: (NudgeSettings) -> NudgeSettings) {
            viewModelScope.launch { settingsRepository.updateSettings(transform) }
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
            const val DEBUG_BEDTIME_OFFSET_HOURS = 12L
        }
    }
