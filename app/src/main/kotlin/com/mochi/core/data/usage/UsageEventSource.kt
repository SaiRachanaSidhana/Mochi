package com.mochi.core.data.usage

import com.mochi.core.model.UsageEvent
import java.time.Instant

interface UsageEventSource {
    fun events(
        from: Instant,
        to: Instant,
        packages: Set<String>,
    ): List<UsageEvent>
}
