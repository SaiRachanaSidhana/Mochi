package com.mochi.core.data.usage

import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.UsageEvent
import com.mochi.core.model.UsageEventType
import com.mochi.core.model.WatchedApps
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class InstagramSessionTrackerTest {
    private val now = Instant.parse("2026-03-10T15:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val instagram = WatchedApps.INSTAGRAM

    private class FakeSource(
        private val events: List<UsageEvent>,
    ) : UsageEventSource {
        var lastQuery: Pair<Instant, Instant>? = null

        override fun events(
            from: Instant,
            to: Instant,
            packages: Set<String>,
        ): List<UsageEvent> {
            lastQuery = from to to
            return events.filter { it.packageName in packages }
        }
    }

    private fun event(
        minutesAgo: Long,
        type: UsageEventType,
        pkg: String = instagram,
    ) = UsageEvent(now.minus(Duration.ofMinutes(minutesAgo)).toEpochMilli(), type, pkg)

    private fun tracker(
        source: UsageEventSource,
        testScope: TestScope,
        watched: WatchedApps = WatchedApps.DEFAULT,
    ) = InstagramSessionTracker(source, clock, watched, StandardTestDispatcher(testScope.testScheduler))

    @Test
    fun `current session is measured from the last resume`() =
        runTest {
            val source = FakeSource(listOf(event(45, UsageEventType.RESUMED)))

            assertThat(tracker(source, this).currentSession()).isEqualTo(Duration.ofMinutes(45))
        }

    @Test
    fun `current session looks back three hours`() =
        runTest {
            val source = FakeSource(emptyList())

            tracker(source, this).currentSession()

            assertThat(source.lastQuery).isEqualTo(now.minus(Duration.ofHours(3)) to now)
        }

    @Test
    fun `no session while Instagram is paused`() =
        runTest {
            val source =
                FakeSource(listOf(event(30, UsageEventType.RESUMED), event(5, UsageEventType.PAUSED)))

            assertThat(tracker(source, this).currentSession()).isNull()
        }

    @Test
    fun `only watched apps count and the watched list is configurable`() =
        runTest {
            val source = FakeSource(listOf(event(20, UsageEventType.RESUMED, pkg = "com.example.tiktok")))

            assertThat(tracker(source, this).currentSession()).isNull()
            assertThat(tracker(source, this, WatchedApps(setOf("com.example.tiktok"))).currentSession())
                .isEqualTo(Duration.ofMinutes(20))
        }

    @Test
    fun `snapshot reports today's total since midnight and the current session`() =
        runTest {
            val source =
                FakeSource(
                    listOf(
                        event(240, UsageEventType.RESUMED),
                        event(220, UsageEventType.PAUSED),
                        event(30, UsageEventType.RESUMED),
                    ),
                )

            val snapshot = tracker(source, this).snapshot()

            assertThat(snapshot.currentSession).isEqualTo(Duration.ofMinutes(30))
            assertThat(snapshot.todayUsage).isEqualTo(Duration.ofMinutes(50))
        }
}
