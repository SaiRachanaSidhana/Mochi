package com.mochi.core.rules

import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.Nudge
import com.mochi.core.model.NudgeSettings
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class NudgeRulesTest {
    private val settings = NudgeSettings.DEFAULT
    private val day = LocalDateTime.of(2026, 3, 10, 15, 0)

    private fun at(
        hour: Int,
        minute: Int,
    ) = LocalDateTime.of(2026, 3, 10, hour, minute)

    private fun decide(
        now: LocalDateTime,
        sessionMinutes: Long = 0,
        lastShownAt: LocalDateTime? = null,
        settings: NudgeSettings = this.settings,
    ) = NudgeRules.decide(now, Duration.ofMinutes(sessionMinutes), lastShownAt, settings)

    @Test
    fun `no nudge during the day for a short session`() {
        assertThat(decide(day, sessionMinutes = 10)).isNull()
    }

    @Test
    fun `bedtime starts at exactly 11 PM but not at 10-59 PM`() {
        assertThat(decide(at(22, 59))).isNull()
        assertThat(decide(at(23, 0))).isEqualTo(Nudge.Bedtime)
    }

    @Test
    fun `bedtime ends at exactly 5 AM but still applies at 4-59 AM`() {
        assertThat(decide(at(4, 59))).isEqualTo(Nudge.Bedtime)
        assertThat(decide(at(5, 0))).isNull()
    }

    @Test
    fun `bedtime applies after midnight`() {
        assertThat(decide(at(0, 30))).isEqualTo(Nudge.Bedtime)
    }

    @Test
    fun `doomscroll nudge needs a full 60 minutes`() {
        assertThat(decide(day, sessionMinutes = 59)).isNull()
        assertThat(decide(day, sessionMinutes = 60)).isEqualTo(Nudge.DoomscrollHour)
        assertThat(decide(day, sessionMinutes = 120)).isEqualTo(Nudge.DoomscrollHour)
    }

    @Test
    fun `bedtime wins when both triggers apply`() {
        assertThat(decide(at(23, 30), sessionMinutes = 90)).isEqualTo(Nudge.Bedtime)
    }

    @Test
    fun `cooldown suppresses nudges for 30 minutes then allows them again`() {
        val shown = day
        assertThat(decide(shown.plusMinutes(29), sessionMinutes = 90, lastShownAt = shown)).isNull()
        assertThat(decide(shown.plusMinutes(30), sessionMinutes = 90, lastShownAt = shown))
            .isEqualTo(Nudge.DoomscrollHour)
    }

    @Test
    fun `cooldown also suppresses the bedtime nudge`() {
        val shown = at(23, 10)
        assertThat(decide(at(23, 39), lastShownAt = shown)).isNull()
        assertThat(decide(at(23, 40), lastShownAt = shown)).isEqualTo(Nudge.Bedtime)
    }

    @Test
    fun `a last-shown time in the future does not block nudges`() {
        assertThat(decide(day, sessionMinutes = 90, lastShownAt = day.plusHours(5)))
            .isEqualTo(Nudge.DoomscrollHour)
    }

    @Test
    fun `custom doomscroll limit is respected`() {
        val custom = settings.copy(doomscrollLimit = Duration.ofMinutes(30))
        assertThat(decide(day, sessionMinutes = 29, settings = custom)).isNull()
        assertThat(decide(day, sessionMinutes = 30, settings = custom)).isEqualTo(Nudge.DoomscrollHour)
    }

    @Test
    fun `custom bedtime and wake time are respected`() {
        val custom = settings.copy(bedtime = LocalTime.of(22, 0), wakeTime = LocalTime.of(7, 0))
        assertThat(decide(at(21, 59), settings = custom)).isNull()
        assertThat(decide(at(22, 0), settings = custom)).isEqualTo(Nudge.Bedtime)
        assertThat(decide(at(6, 59), settings = custom)).isEqualTo(Nudge.Bedtime)
        assertThat(decide(at(7, 0), settings = custom)).isNull()
    }

    @Test
    fun `custom bedtime window that does not wrap midnight`() {
        val custom = settings.copy(bedtime = LocalTime.of(13, 0), wakeTime = LocalTime.of(14, 0))
        assertThat(decide(at(12, 59), settings = custom)).isNull()
        assertThat(decide(at(13, 30), settings = custom)).isEqualTo(Nudge.Bedtime)
        assertThat(decide(at(14, 0), settings = custom)).isNull()
    }

    @Test
    fun `custom cooldown is respected`() {
        val custom = settings.copy(cooldown = Duration.ofMinutes(15))
        assertThat(decide(day.plusMinutes(14), sessionMinutes = 90, lastShownAt = day, settings = custom)).isNull()
        assertThat(decide(day.plusMinutes(15), sessionMinutes = 90, lastShownAt = day, settings = custom))
            .isEqualTo(Nudge.DoomscrollHour)
    }

    @Test
    fun `an empty bedtime window never triggers`() {
        val custom = settings.copy(bedtime = LocalTime.of(23, 0), wakeTime = LocalTime.of(23, 0))
        assertThat(decide(at(23, 30), settings = custom)).isNull()
    }
}
