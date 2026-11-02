package com.mochi.core.model

enum class UsageEventType {
    RESUMED,

    PAUSED,
}

data class UsageEvent(
    val timestampMillis: Long,
    val type: UsageEventType,
    val packageName: String,
)

data class WatchedApps(
    val packages: Set<String>,
) {
    companion object {
        const val INSTAGRAM = "com.instagram.android"

        val DEFAULT = WatchedApps(setOf(INSTAGRAM))
    }
}
