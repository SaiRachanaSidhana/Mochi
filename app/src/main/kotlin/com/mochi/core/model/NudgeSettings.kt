package com.mochi.core.model

import java.time.Duration
import java.time.LocalTime

data class NudgeSettings(
    val doomscrollLimit: Duration = Duration.ofMinutes(DEFAULT_DOOMSCROLL_LIMIT_MINUTES),
    val bedtime: LocalTime = LocalTime.of(DEFAULT_BEDTIME_HOUR, 0),
    val wakeTime: LocalTime = LocalTime.of(DEFAULT_WAKE_HOUR, 0),
    val cooldown: Duration = Duration.ofMinutes(DEFAULT_COOLDOWN_MINUTES),
    val lockEnabled: Boolean = true,
) {
    companion object {
        private const val DEFAULT_DOOMSCROLL_LIMIT_MINUTES = 60L
        private const val DEFAULT_BEDTIME_HOUR = 23
        private const val DEFAULT_WAKE_HOUR = 5
        private const val DEFAULT_COOLDOWN_MINUTES = 30L

        val DEFAULT = NudgeSettings()

        val DOOMSCROLL_LIMIT_OPTIONS_MINUTES = listOf(30, 45, 60, 90, 120)

        val COOLDOWN_OPTIONS_MINUTES = listOf(15, 30, 60)
    }
}
