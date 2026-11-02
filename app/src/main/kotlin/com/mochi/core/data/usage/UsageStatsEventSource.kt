package com.mochi.core.data.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.mochi.core.model.UsageEvent
import com.mochi.core.model.UsageEventType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

class UsageStatsEventSource
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : UsageEventSource {
        override fun events(
            from: Instant,
            to: Instant,
            packages: Set<String>,
        ): List<UsageEvent> {
            val usageStatsManager = context.getSystemService(UsageStatsManager::class.java) ?: return emptyList()
            val queried = usageStatsManager.queryEvents(from.toEpochMilli(), to.toEpochMilli())
            val raw = UsageEvents.Event()
            val result = mutableListOf<UsageEvent>()
            while (queried.hasNextEvent()) {
                queried.getNextEvent(raw)
                val type = raw.eventType.toUsageEventType()
                if (type != null && raw.packageName in packages) {
                    result += UsageEvent(raw.timeStamp, type, raw.packageName)
                }
            }
            return result
        }

        private fun Int.toUsageEventType(): UsageEventType? =
            when (this) {
                UsageEvents.Event.ACTIVITY_RESUMED -> UsageEventType.RESUMED
                UsageEvents.Event.ACTIVITY_PAUSED -> UsageEventType.PAUSED
                else -> null
            }
    }
