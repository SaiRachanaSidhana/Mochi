package com.mochi.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.mochi.core.model.NudgeSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : SettingsRepository {
        private val preferences: Flow<Preferences> =
            dataStore.data.catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }

        override val settings: Flow<NudgeSettings> =
            preferences.map { it.toSettings() }.distinctUntilChanged()

        override val floatingMochiEnabled: Flow<Boolean> =
            preferences.map { it[Keys.FLOATING_ENABLED] ?: false }.distinctUntilChanged()

        override val lastNudgeAt: Flow<LocalDateTime?> =
            preferences
                .map { prefs ->
                    prefs[Keys.LAST_NUDGE_AT]?.let {
                        try {
                            LocalDateTime.parse(it)
                        } catch (_: DateTimeParseException) {
                            null
                        }
                    }
                }.distinctUntilChanged()

        override val instagramLocked: Flow<Boolean> =
            preferences.map { it[Keys.INSTAGRAM_LOCKED] ?: false }.distinctUntilChanged()

        override suspend fun setInstagramLocked(locked: Boolean) {
            dataStore.edit { it[Keys.INSTAGRAM_LOCKED] = locked }
        }

        override val usedMessageIds: Flow<Set<String>> =
            preferences.map { it[Keys.USED_MESSAGE_IDS] ?: emptySet() }.distinctUntilChanged()

        override suspend fun updateUsedMessageIds(transform: (Set<String>) -> Set<String>) {
            dataStore.edit { prefs ->
                prefs[Keys.USED_MESSAGE_IDS] = transform(prefs[Keys.USED_MESSAGE_IDS] ?: emptySet())
            }
        }

        override suspend fun updateSettings(transform: (NudgeSettings) -> NudgeSettings) {
            dataStore.edit { prefs ->
                val updated = transform(prefs.toSettings())
                prefs[Keys.DOOMSCROLL_LIMIT_MINUTES] = updated.doomscrollLimit.toMinutes().toInt()
                prefs[Keys.BEDTIME_MINUTE_OF_DAY] = updated.bedtime.toMinuteOfDay()
                prefs[Keys.WAKE_MINUTE_OF_DAY] = updated.wakeTime.toMinuteOfDay()
                prefs[Keys.COOLDOWN_MINUTES] = updated.cooldown.toMinutes().toInt()
                prefs[Keys.LOCK_ENABLED] = updated.lockEnabled
            }
        }

        override suspend fun setFloatingMochiEnabled(enabled: Boolean) {
            dataStore.edit { it[Keys.FLOATING_ENABLED] = enabled }
        }

        override suspend fun recordNudge(at: LocalDateTime) {
            dataStore.edit { it[Keys.LAST_NUDGE_AT] = at.toString() }
        }

        override suspend fun resetToDefaults() {
            updateSettings { NudgeSettings.DEFAULT }
        }

        private fun Preferences.toSettings(): NudgeSettings {
            val defaults = NudgeSettings.DEFAULT
            return NudgeSettings(
                doomscrollLimit = this[Keys.DOOMSCROLL_LIMIT_MINUTES]?.toDuration() ?: defaults.doomscrollLimit,
                bedtime = this[Keys.BEDTIME_MINUTE_OF_DAY]?.toLocalTime() ?: defaults.bedtime,
                wakeTime = this[Keys.WAKE_MINUTE_OF_DAY]?.toLocalTime() ?: defaults.wakeTime,
                cooldown = this[Keys.COOLDOWN_MINUTES]?.toDuration() ?: defaults.cooldown,
                lockEnabled = this[Keys.LOCK_ENABLED] ?: defaults.lockEnabled,
            )
        }

        private fun Int.toDuration(): Duration = Duration.ofMinutes(coerceAtLeast(1).toLong())

        private fun Int.toLocalTime(): LocalTime {
            val minuteOfDay = coerceIn(0, MINUTES_PER_DAY - 1)
            return LocalTime.of(minuteOfDay / MINUTES_PER_HOUR, minuteOfDay % MINUTES_PER_HOUR)
        }

        private fun LocalTime.toMinuteOfDay(): Int = hour * MINUTES_PER_HOUR + minute

        private object Keys {
            val DOOMSCROLL_LIMIT_MINUTES = intPreferencesKey("doomscroll_limit_minutes")
            val BEDTIME_MINUTE_OF_DAY = intPreferencesKey("bedtime_minute_of_day")
            val WAKE_MINUTE_OF_DAY = intPreferencesKey("wake_minute_of_day")
            val COOLDOWN_MINUTES = intPreferencesKey("cooldown_minutes")
            val FLOATING_ENABLED = booleanPreferencesKey("floating_mochi_enabled")
            val LAST_NUDGE_AT = stringPreferencesKey("last_nudge_at")
            val USED_MESSAGE_IDS = stringSetPreferencesKey("used_message_ids")
            val LOCK_ENABLED = booleanPreferencesKey("lock_enabled")
            val INSTAGRAM_LOCKED = booleanPreferencesKey("instagram_locked")
        }

        private companion object {
            const val MINUTES_PER_HOUR = 60
            const val MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR
        }
    }
