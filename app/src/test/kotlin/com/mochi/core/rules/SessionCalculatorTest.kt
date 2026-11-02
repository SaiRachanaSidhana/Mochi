package com.mochi.core.rules

import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.UsageEvent
import com.mochi.core.model.UsageEventType
import org.junit.Test
import java.time.Duration
import java.time.Instant

class SessionCalculatorTest {
    private val instagram = "com.instagram.android"
    private val whatsapp = "com.whatsapp"
    private val watched = setOf(instagram)
    private val origin = Instant.parse("2026-03-10T10:00:00Z")

    private fun minute(offset: Long): Long = origin.plus(Duration.ofMinutes(offset)).toEpochMilli()

    private fun resumed(
        minute: Long,
        pkg: String = instagram,
    ) = UsageEvent(minute(minute), UsageEventType.RESUMED, pkg)

    private fun paused(
        minute: Long,
        pkg: String = instagram,
    ) = UsageEvent(minute(minute), UsageEventType.PAUSED, pkg)

    private fun session(
        events: List<UsageEvent>,
        nowMinute: Long,
    ) = SessionCalculator.currentSession(
        events = events,
        now = origin.plus(Duration.ofMinutes(nowMinute)),
        watchedPackages = watched,
    )

    @Test
    fun `no events means no session`() {
        assertThat(session(emptyList(), nowMinute = 10)).isNull()
    }

    @Test
    fun `one continuous session is measured until now`() {
        assertThat(session(listOf(resumed(0)), nowMinute = 45)).isEqualTo(Duration.ofMinutes(45))
    }

    @Test
    fun `returns null when Instagram is currently paused`() {
        assertThat(session(listOf(resumed(0), paused(30)), nowMinute = 31)).isNull()
    }

    @Test
    fun `a 1 minute break continues the session`() {
        val events = listOf(resumed(0), paused(20), resumed(21))
        assertThat(session(events, nowMinute = 40)).isEqualTo(Duration.ofMinutes(40))
    }

    @Test
    fun `a break of exactly 2 minutes still continues the session`() {
        val events = listOf(resumed(0), paused(20), resumed(22))
        assertThat(session(events, nowMinute = 40)).isEqualTo(Duration.ofMinutes(40))
    }

    @Test
    fun `a 3 minute break starts a new session`() {
        val events = listOf(resumed(0), paused(20), resumed(23))
        assertThat(session(events, nowMinute = 40)).isEqualTo(Duration.ofMinutes(17))
    }

    @Test
    fun `switching to another app for 1 minute does not reset the session`() {
        val events =
            listOf(
                resumed(0),
                paused(30),
                resumed(30, whatsapp),
                paused(31, whatsapp),
                resumed(31),
            )
        assertThat(session(events, nowMinute = 50)).isEqualTo(Duration.ofMinutes(50))
    }

    @Test
    fun `events from other apps are ignored`() {
        val events = listOf(resumed(0, whatsapp), resumed(5), paused(20), resumed(30, whatsapp))
        assertThat(session(events, nowMinute = 40)).isNull()
        assertThat(session(listOf(resumed(0, whatsapp)), nowMinute = 40)).isNull()
    }

    @Test
    fun `multiple resume and pause pairs inside Instagram form one session`() {
        val events =
            listOf(
                resumed(0),
                paused(10),
                resumed(10),
                paused(25),
                resumed(26),
                paused(40),
                resumed(40),
            )
        assertThat(session(events, nowMinute = 55)).isEqualTo(Duration.ofMinutes(55))
    }

    @Test
    fun `an activity hand-over where the new resume comes before the old pause stays in the foreground`() {
        val events = listOf(resumed(0), resumed(10), paused(10), paused(11))
        assertThat(session(events, nowMinute = 20)).isNull()
        assertThat(session(listOf(resumed(0), resumed(10), paused(10)), nowMinute = 20))
            .isEqualTo(Duration.ofMinutes(20))
    }

    @Test
    fun `unsorted events are handled`() {
        val events = listOf(resumed(21), resumed(0), paused(20))
        assertThat(session(events, nowMinute = 30)).isEqualTo(Duration.ofMinutes(30))
    }

    @Test
    fun `a pause without a matching resume in the window is ignored`() {
        val events = listOf(paused(1), resumed(5))
        assertThat(session(events, nowMinute = 15)).isEqualTo(Duration.ofMinutes(10))
    }

    @Test
    fun `custom break tolerance is honoured`() {
        val events = listOf(resumed(0), paused(10), resumed(15))
        val now = origin.plus(Duration.ofMinutes(30))
        assertThat(SessionCalculator.currentSession(events, now, watched, Duration.ofMinutes(5)))
            .isEqualTo(Duration.ofMinutes(30))
        assertThat(SessionCalculator.currentSession(events, now, watched, Duration.ofMinutes(4)))
            .isEqualTo(Duration.ofMinutes(15))
    }

    @Test
    fun `total foreground time sums intervals and ignores breaks`() {
        val events = listOf(resumed(0), paused(10), resumed(20), paused(25), resumed(40))
        val total =
            SessionCalculator.totalForegroundTime(
                events = events,
                from = origin,
                now = origin.plus(Duration.ofMinutes(50)),
                watchedPackages = watched,
            )
        assertThat(total).isEqualTo(Duration.ofMinutes(25))
    }

    @Test
    fun `total foreground time is clipped to the start of the range`() {
        val events = listOf(resumed(0), paused(30))
        val total =
            SessionCalculator.totalForegroundTime(
                events = events,
                from = origin.plus(Duration.ofMinutes(20)),
                now = origin.plus(Duration.ofMinutes(60)),
                watchedPackages = watched,
            )
        assertThat(total).isEqualTo(Duration.ofMinutes(10))
    }
}
