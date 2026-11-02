package com.mochi.core.data.usage

import com.mochi.core.data.di.IoDispatcher
import com.mochi.core.model.WatchedApps
import com.mochi.core.rules.SessionCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Duration
import javax.inject.Inject

class InstagramSessionTracker
    @Inject
    constructor(
        private val eventSource: UsageEventSource,
        private val clock: Clock,
        private val watchedApps: WatchedApps,
        @IoDispatcher private val dispatcher: CoroutineDispatcher,
    ) : SessionTracker {
        override suspend fun currentSession(): Duration? =
            withContext(dispatcher) {
                val now = clock.instant()
                val events = eventSource.events(now.minus(QUERY_WINDOW), now, watchedApps.packages)
                SessionCalculator.currentSession(events, now, watchedApps.packages)
            }

        override suspend fun snapshot(): UsageSnapshot =
            withContext(dispatcher) {
                val now = clock.instant()
                val startOfToday =
                    now
                        .atZone(clock.zone)
                        .toLocalDate()
                        .atStartOfDay(clock.zone)
                        .toInstant()
                val from = minOf(startOfToday, now.minus(QUERY_WINDOW))
                val events = eventSource.events(from, now, watchedApps.packages)
                UsageSnapshot(
                    currentSession = SessionCalculator.currentSession(events, now, watchedApps.packages),
                    todayUsage = SessionCalculator.totalForegroundTime(events, startOfToday, now, watchedApps.packages),
                )
            }

        private companion object {
            val QUERY_WINDOW: Duration = Duration.ofHours(3)
        }
    }
