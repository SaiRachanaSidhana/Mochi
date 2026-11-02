package com.mochi.feature.settings

import com.mochi.core.model.NudgeSettings

data class SettingsUiState(
    val settings: NudgeSettings = NudgeSettings.DEFAULT,
)
