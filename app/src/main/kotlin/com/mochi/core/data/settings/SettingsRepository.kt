package com.mochi.core.data.settings

import com.mochi.core.model.NudgeSettings
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface SettingsRepository {
    val settings: Flow<NudgeSettings>

    val floatingMochiEnabled: Flow<Boolean>

    val lastNudgeAt: Flow<LocalDateTime?>

    val usedMessageIds: Flow<Set<String>>

    val instagramLocked: Flow<Boolean>

    suspend fun setInstagramLocked(locked: Boolean)

    suspend fun updateSettings(transform: (NudgeSettings) -> NudgeSettings)

    suspend fun updateUsedMessageIds(transform: (Set<String>) -> Set<String>)

    suspend fun setFloatingMochiEnabled(enabled: Boolean)

    suspend fun recordNudge(at: LocalDateTime)

    suspend fun resetToDefaults()
}
