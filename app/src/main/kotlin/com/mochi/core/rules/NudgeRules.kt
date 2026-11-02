package com.mochi.core.rules

import com.mochi.core.model.Nudge
import com.mochi.core.model.NudgeSettings
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

object NudgeRules {
    fun decide(
        now: LocalDateTime,
        session: Duration,
        lastShownAt: LocalDateTime?,
        settings: NudgeSettings,
    ): Nudge? =
        when {
            isCoolingDown(now, lastShownAt, settings.cooldown) -> null
            isBedtime(now.toLocalTime(), settings.bedtime, settings.wakeTime) -> Nudge.Bedtime
            session >= settings.doomscrollLimit -> Nudge.DoomscrollHour
            else -> null
        }

    fun isBedtime(
        time: LocalTime,
        bedtime: LocalTime,
        wakeTime: LocalTime,
    ): Boolean =
        when {
            bedtime == wakeTime -> false
            bedtime < wakeTime -> time >= bedtime && time < wakeTime
            else -> time >= bedtime || time < wakeTime
        }

    private fun isCoolingDown(
        now: LocalDateTime,
        lastShownAt: LocalDateTime?,
        cooldown: Duration,
    ): Boolean {
        if (lastShownAt == null) return false
        val elapsed = Duration.between(lastShownAt, now)
        // clock went backwards, ignore it
        return !elapsed.isNegative && elapsed < cooldown
    }
}
