package com.mochi.core.data.usage

import java.time.Duration

data class UsageSnapshot(
    val currentSession: Duration?,
    val todayUsage: Duration,
)

interface SessionTracker {
    suspend fun currentSession(): Duration?

    suspend fun snapshot(): UsageSnapshot
}
