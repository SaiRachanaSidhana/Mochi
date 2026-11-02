package com.mochi.feature.overlay

import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.MochiMessages
import com.mochi.core.model.Nudge
import com.mochi.core.testing.FakeSessionTracker
import com.mochi.core.testing.FakeSettingsRepository
import com.mochi.core.testing.MutableClock
import com.mochi.feature.lock.LockLauncher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

class NudgeWatcherTest {
    private class RecordingPresenter(
        var accepts: Boolean = true,
    ) : NudgePresenter {
        val shown = mutableListOf<Nudge>()
        val messages = mutableListOf<String>()

        override val isShowing: Boolean = false

        override fun show(
            nudge: Nudge,
            message: String,
        ): Boolean {
            shown += nudge
            if (accepts) messages += message
            return accepts
        }
    }

    private class RecordingLockLauncher : LockLauncher {
        var shownCount = 0

        override fun show(): Boolean {
            shownCount++
            return true
        }
    }

    // 15:00 UTC, outside bedtime
    private val clock = MutableClock(Instant.parse("2026-03-10T15:00:00Z"))
    private val tracker = FakeSessionTracker()
    private val settings = FakeSettingsRepository()
    private val presenter = RecordingPresenter()
    private val lockLauncher = RecordingLockLauncher()
    private val watcher = NudgeWatcher(tracker, settings, presenter, clock, lockLauncher)

    @Test
    fun `after the fifth visit Instagram is locked and the lock screen opens`() =
        runTest {
            tracker.session = Duration.ofMinutes(60)

            watcher.tick()

            assertThat(settings.isLocked).isTrue()
            assertThat(lockLauncher.shownCount).isEqualTo(1)
        }

    @Test
    fun `nothing is locked when the lock option is off`() =
        runTest {
            settings.updateSettings { it.copy(lockEnabled = false) }
            tracker.session = Duration.ofMinutes(60)

            watcher.tick()

            assertThat(presenter.shown).hasSize(MochiMessages.VISITS)
            assertThat(settings.isLocked).isFalse()
            assertThat(lockLauncher.shownCount).isEqualTo(0)
        }

    @Test
    fun `a locked app gets the lock screen again instead of new visits`() =
        runTest {
            settings.setInstagramLocked(true)
            tracker.session = Duration.ofMinutes(5)

            assertThat(watcher.tick()).isTrue()

            assertThat(lockLauncher.shownCount).isEqualTo(1)
            assertThat(presenter.shown).isEmpty()
        }

    @Test
    fun `turning the lock option off clears an existing lock`() =
        runTest {
            settings.setInstagramLocked(true)
            settings.updateSettings { it.copy(lockEnabled = false) }
            tracker.session = Duration.ofMinutes(5)

            watcher.tick()

            assertThat(settings.isLocked).isFalse()
            assertThat(lockLauncher.shownCount).isEqualTo(0)
        }

    @Test
    fun `the lock is only checked while the app is in the foreground`() =
        runTest {
            settings.setInstagramLocked(true)
            tracker.session = null

            assertThat(watcher.tick()).isFalse()
            assertThat(lockLauncher.shownCount).isEqualTo(0)
        }

    @Test
    fun `does nothing while the watched app is not in the foreground`() =
        runTest {
            tracker.session = null

            assertThat(watcher.tick()).isFalse()
            assertThat(presenter.shown).isEmpty()
        }

    @Test
    fun `a short session reports foreground but shows nothing`() =
        runTest {
            tracker.session = Duration.ofMinutes(10)

            assertThat(watcher.tick()).isTrue()
            assertThat(presenter.shown).isEmpty()
        }

    @Test
    fun `a 60 minute session shows five doomscroll visits and records the time`() =
        runTest {
            tracker.session = Duration.ofMinutes(60)

            assertThat(watcher.tick()).isTrue()

            assertThat(presenter.shown).containsExactly(*Array(MochiMessages.VISITS) { Nudge.DoomscrollHour })
            assertThat(settings.lastRecordedNudge).isEqualTo(LocalDateTime.of(2026, 3, 10, 15, 0))
        }

    @Test
    fun `the five visits all say different things`() =
        runTest {
            tracker.session = Duration.ofMinutes(60)

            watcher.tick()

            assertThat(presenter.messages).hasSize(MochiMessages.VISITS)
            assertThat(presenter.messages.toSet()).hasSize(MochiMessages.VISITS)
        }

    @Test
    fun `lines used today are not used again the next day`() =
        runTest {
            settings.updateSettings { it.copy(lockEnabled = false) }
            tracker.session = Duration.ofMinutes(60)
            watcher.tick()
            val today = presenter.messages.toList()

            clock.advance(Duration.ofDays(1))
            watcher.tick()
            val tomorrow = presenter.messages.drop(MochiMessages.VISITS)

            assertThat(tomorrow).hasSize(MochiMessages.VISITS)
            assertThat(tomorrow.intersect(today.toSet())).isEmpty()
        }

    @Test
    fun `leaving the app mid sequence stops the remaining visits`() =
        runTest {
            tracker.session = Duration.ofMinutes(60)
            val leaving =
                object : NudgePresenter by presenter {
                    override fun show(
                        nudge: Nudge,
                        message: String,
                    ): Boolean {
                        tracker.session = null
                        return presenter.show(nudge, message)
                    }
                }

            NudgeWatcher(tracker, settings, leaving, clock, lockLauncher).tick()

            assertThat(presenter.shown).hasSize(1)
            assertThat(settings.isLocked).isFalse()
        }

    @Test
    fun `the cooldown holds Mochi back for 30 minutes then lets it return`() =
        runTest {
            settings.updateSettings { it.copy(lockEnabled = false) }
            tracker.session = Duration.ofMinutes(75)
            watcher.tick()

            clock.advance(Duration.ofMinutes(29))
            watcher.tick()
            assertThat(presenter.shown).hasSize(MochiMessages.VISITS)

            clock.advance(Duration.ofMinutes(1))
            watcher.tick()
            assertThat(presenter.shown).hasSize(MochiMessages.VISITS * 2)
        }

    @Test
    fun `bedtime wins over doomscroll late at night`() =
        runTest {
            clock.advance(Duration.ofHours(8) + Duration.ofMinutes(30)) // 23:30
            tracker.session = Duration.ofMinutes(90)

            watcher.tick()

            assertThat(presenter.shown).containsExactly(*Array(MochiMessages.VISITS) { Nudge.Bedtime })
        }

    @Test
    fun `a refused presentation is not recorded so it is retried next tick`() =
        runTest {
            tracker.session = Duration.ofMinutes(90)
            presenter.accepts = false

            watcher.tick()
            assertThat(settings.lastRecordedNudge).isNull()
            assertThat(presenter.shown).hasSize(1)

            presenter.accepts = true
            watcher.tick()
            assertThat(presenter.shown).hasSize(1 + MochiMessages.VISITS)
            assertThat(settings.lastRecordedNudge).isNotNull()
        }

    @Test
    fun `custom settings are respected`() =
        runTest {
            settings.updateSettings { it.copy(doomscrollLimit = Duration.ofMinutes(1)) }
            tracker.session = Duration.ofMinutes(1)

            watcher.tick()

            assertThat(presenter.shown).containsExactly(*Array(MochiMessages.VISITS) { Nudge.DoomscrollHour })
        }
}
