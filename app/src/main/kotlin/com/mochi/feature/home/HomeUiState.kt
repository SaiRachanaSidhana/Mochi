package com.mochi.feature.home

import java.time.Duration

data class HomeUiState(
    val floatingMochiEnabled: Boolean = false,
    val todayUsage: Duration = Duration.ZERO,
    val currentSession: Duration? = null,
    val isLoading: Boolean = true,
)
