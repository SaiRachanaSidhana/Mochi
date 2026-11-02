package com.mochi.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochi.core.data.FloatingMochiController
import com.mochi.core.data.settings.SettingsRepository
import com.mochi.core.data.usage.SessionTracker
import com.mochi.core.data.usage.UsageSnapshot
import com.mochi.core.model.Nudge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val tracker: SessionTracker,
        private val controller: FloatingMochiController,
    ) : ViewModel() {
        private var nextPreviewIsDoomscroll = true

        val uiState: StateFlow<HomeUiState> =
            combine(settingsRepository.floatingMochiEnabled, usageSnapshots()) { enabled, snapshot ->
                HomeUiState(
                    floatingMochiEnabled = enabled,
                    todayUsage = snapshot.todayUsage,
                    currentSession = snapshot.currentSession,
                    isLoading = false,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomeUiState())

        init {
            // restart the service if it died while the toggle is on
            viewModelScope.launch {
                if (settingsRepository.floatingMochiEnabled.first()) controller.start()
            }
        }

        fun onFloatingMochiToggled(enabled: Boolean) {
            viewModelScope.launch {
                settingsRepository.setFloatingMochiEnabled(enabled)
                if (enabled) controller.start() else controller.stop()
            }
        }

        fun onPreviewClicked() {
            val nudge = if (nextPreviewIsDoomscroll) Nudge.DoomscrollHour else Nudge.Bedtime
            if (controller.preview(nudge)) nextPreviewIsDoomscroll = !nextPreviewIsDoomscroll
        }

        private fun usageSnapshots(): Flow<UsageSnapshot> =
            flow {
                while (true) {
                    emit(
                        try {
                            tracker.snapshot()
                        } catch (_: SecurityException) {
                            UsageSnapshot(currentSession = null, todayUsage = Duration.ZERO)
                        },
                    )
                    delay(REFRESH_INTERVAL_MILLIS)
                }
            }

        companion object {
            const val REFRESH_INTERVAL_MILLIS = 60_000L
            private const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
