package com.mochi.core.testing

import com.mochi.core.data.FloatingMochiController
import com.mochi.core.data.permissions.PermissionChecker
import com.mochi.core.data.permissions.PermissionState
import com.mochi.core.data.settings.SettingsRepository
import com.mochi.core.data.usage.SessionTracker
import com.mochi.core.data.usage.UsageSnapshot
import com.mochi.core.model.Nudge
import com.mochi.core.model.NudgeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class FakeSettingsRepository(
    initial: NudgeSettings = NudgeSettings.DEFAULT,
    enabled: Boolean = false,
) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    private val enabledState = MutableStateFlow(enabled)
    private val lastNudge = MutableStateFlow<LocalDateTime?>(null)

    override val settings: Flow<NudgeSettings> = state
    override val floatingMochiEnabled: Flow<Boolean> = enabledState
    override val lastNudgeAt: Flow<LocalDateTime?> = lastNudge.map { it }

    val currentSettings: NudgeSettings get() = state.value
    val isEnabled: Boolean get() = enabledState.value
    val lastRecordedNudge: LocalDateTime? get() = lastNudge.value

    private val lockedState = MutableStateFlow(false)

    override val instagramLocked: Flow<Boolean> = lockedState

    val isLocked: Boolean get() = lockedState.value

    override suspend fun setInstagramLocked(locked: Boolean) {
        lockedState.value = locked
    }

    private val usedIds = MutableStateFlow<Set<String>>(emptySet())

    override val usedMessageIds: Flow<Set<String>> = usedIds

    val currentUsedMessageIds: Set<String> get() = usedIds.value

    override suspend fun updateUsedMessageIds(transform: (Set<String>) -> Set<String>) {
        usedIds.update(transform)
    }

    override suspend fun updateSettings(transform: (NudgeSettings) -> NudgeSettings) {
        state.update(transform)
    }

    override suspend fun setFloatingMochiEnabled(enabled: Boolean) {
        enabledState.value = enabled
    }

    override suspend fun recordNudge(at: LocalDateTime) {
        lastNudge.value = at
    }

    override suspend fun resetToDefaults() {
        state.value = NudgeSettings.DEFAULT
    }

    fun setLastNudge(at: LocalDateTime?) {
        lastNudge.value = at
    }
}

class FakeSessionTracker(
    var session: Duration? = null,
    var todayUsage: Duration = Duration.ZERO,
) : SessionTracker {
    var queries = 0
        private set

    override suspend fun currentSession(): Duration? {
        queries++
        return session
    }

    override suspend fun snapshot(): UsageSnapshot {
        queries++
        return UsageSnapshot(session, todayUsage)
    }
}

class FakeFloatingMochiController(
    var previewResult: Boolean = true,
) : FloatingMochiController {
    var startCount = 0
        private set
    var stopCount = 0
        private set
    val previews = mutableListOf<Nudge>()

    override fun start() {
        startCount++
    }

    override fun stop() {
        stopCount++
    }

    override fun preview(nudge: Nudge): Boolean {
        previews += nudge
        return previewResult
    }
}

class FakePermissionChecker(
    var state: PermissionState =
        PermissionState(
            usageAccess = false,
            overlay = false,
            notifications = false,
            notificationPermissionApplicable = true,
        ),
) : PermissionChecker {
    override fun check(): PermissionState = state
}

class MutableClock(
    private var current: Instant,
    private val zone: ZoneId = ZoneOffset.UTC,
) : Clock() {
    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClock(current, zone)

    override fun instant(): Instant = current

    fun advance(duration: Duration) {
        current = current.plus(duration)
    }
}
