package com.mochi.feature.overlay

import com.mochi.core.data.settings.SettingsRepository
import com.mochi.core.data.usage.SessionTracker
import com.mochi.core.model.MochiMessages
import com.mochi.core.model.Nudge
import com.mochi.core.rules.MessagePicker
import com.mochi.core.rules.NudgeRules
import com.mochi.feature.lock.LockLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class NudgeWatcher
    @Inject
    constructor(
        private val tracker: SessionTracker,
        private val settingsRepository: SettingsRepository,
        private val presenter: NudgePresenter,
        private val clock: Clock,
        private val lockLauncher: LockLauncher,
    ) {
        suspend fun isLocked(): Boolean = settingsRepository.instagramLocked.first()

        suspend fun tick(): Boolean {
            val session = tracker.currentSession() ?: return false
            val settings = settingsRepository.settings.first()

            if (isLocked()) {
                // locked: show the lock screen, no new visits
                if (settings.lockEnabled) {
                    lockLauncher.show()
                    return true
                }
                settingsRepository.setInstagramLocked(false)
            }

            val lastShownAt = settingsRepository.lastNudgeAt.first()
            val now = LocalDateTime.now(clock)

            val nudge = NudgeRules.decide(now, session, lastShownAt, settings)
            if (nudge != null && showVisit(nudge, visit = 1)) {
                settingsRepository.recordNudge(now)
                val finished = runRemainingVisits(nudge)
                if (finished && settings.lockEnabled) lockInstagram()
            }
            return true
        }

        private suspend fun runRemainingVisits(nudge: Nudge): Boolean {
            for (visit in 2..MochiMessages.VISITS) {
                awaitFinished()
                delay(VISIT_GAP)
                // user left, stop the sequence
                if (tracker.currentSession() == null || !showVisit(nudge, visit)) return false
            }
            awaitFinished()
            return tracker.currentSession() != null
        }

        private suspend fun awaitFinished() {
            while (presenter.isShowing) delay(FINISH_POLL)
        }

        private suspend fun lockInstagram() {
            settingsRepository.setInstagramLocked(true)
            lockLauncher.show()
        }

        private suspend fun showVisit(
            nudge: Nudge,
            visit: Int,
        ): Boolean {
            val pool = MochiMessages.pool(nudge, visit)
            var used = settingsRepository.usedMessageIds.first()
            if (MessagePicker.exhausted(pool, used)) {
                val poolIds = pool.map { it.id }.toSet()
                settingsRepository.updateUsedMessageIds { it - poolIds }
                used = used - poolIds
            }
            val message = MessagePicker.pick(pool, used)
            if (!presenter.show(nudge, message.text)) return false
            settingsRepository.updateUsedMessageIds { it + message.id }
            return true
        }

        private companion object {
            val VISIT_GAP = 3.seconds
            val FINISH_POLL = 250.milliseconds
        }
    }
