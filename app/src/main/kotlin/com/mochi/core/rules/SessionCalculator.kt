package com.mochi.core.rules

import com.mochi.core.model.UsageEvent
import com.mochi.core.model.UsageEventType
import java.time.Duration
import java.time.Instant

object SessionCalculator {
    val DEFAULT_BREAK_TOLERANCE: Duration = Duration.ofMinutes(2)

    data class ForegroundInterval(
        val start: Instant,
        val end: Instant?,
    ) {
        val isOpen: Boolean get() = end == null
    }

    fun currentSession(
        events: List<UsageEvent>,
        now: Instant,
        watchedPackages: Set<String>,
        breakTolerance: Duration = DEFAULT_BREAK_TOLERANCE,
    ): Duration? {
        val lastSession = mergeSessions(foregroundIntervals(events, watchedPackages), breakTolerance).lastOrNull()
        return if (lastSession != null && lastSession.isOpen) Duration.between(lastSession.start, now) else null
    }

    fun totalForegroundTime(
        events: List<UsageEvent>,
        from: Instant,
        now: Instant,
        watchedPackages: Set<String>,
    ): Duration =
        foregroundIntervals(events, watchedPackages)
            .map { interval ->
                val start = maxOf(interval.start, from)
                val end = minOf(interval.end ?: now, now)
                if (end.isAfter(start)) Duration.between(start, end) else Duration.ZERO
            }.fold(Duration.ZERO, Duration::plus)

    fun foregroundIntervals(
        events: List<UsageEvent>,
        watchedPackages: Set<String>,
    ): List<ForegroundInterval> {
        val openActivities = mutableMapOf<String, Int>()
        val intervals = mutableListOf<ForegroundInterval>()
        var openedAt: Instant? = null

        events
            .filter { it.packageName in watchedPackages }
            .sortedBy { it.timestampMillis }
            .forEach { event ->
                val wasForeground = openActivities.values.any { it > 0 }
                val count = openActivities.getOrDefault(event.packageName, 0)
                openActivities[event.packageName] =
                    when (event.type) {
                        UsageEventType.RESUMED -> count + 1
                        UsageEventType.PAUSED -> maxOf(0, count - 1)
                    }
                val isForeground = openActivities.values.any { it > 0 }
                val at = Instant.ofEpochMilli(event.timestampMillis)

                if (!wasForeground && isForeground) {
                    openedAt = at
                } else if (wasForeground && !isForeground) {
                    intervals += ForegroundInterval(checkNotNull(openedAt), at)
                    openedAt = null
                }
            }

        openedAt?.let { intervals += ForegroundInterval(it, null) }
        return intervals
    }

    private fun mergeSessions(
        intervals: List<ForegroundInterval>,
        breakTolerance: Duration,
    ): List<ForegroundInterval> {
        val sessions = mutableListOf<ForegroundInterval>()
        intervals.forEach { next ->
            val previous = sessions.lastOrNull()
            val previousEnd = previous?.end
            if (previous != null && previousEnd != null &&
                Duration.between(previousEnd, next.start) <= breakTolerance
            ) {
                sessions[sessions.lastIndex] = ForegroundInterval(previous.start, next.end)
            } else {
                sessions += next
            }
        }
        return sessions
    }
}
