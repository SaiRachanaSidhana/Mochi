package com.mochi.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mochi.app.R
import java.time.Duration

private const val MINUTES_PER_HOUR = 60

@Composable
internal fun formatDuration(duration: Duration): String {
    val totalMinutes = duration.toMinutes()
    return when {
        totalMinutes >= MINUTES_PER_HOUR -> {
            stringResource(
                R.string.duration_hours_minutes,
                totalMinutes / MINUTES_PER_HOUR,
                totalMinutes % MINUTES_PER_HOUR,
            )
        }

        totalMinutes >= 1 -> {
            stringResource(R.string.duration_minutes, totalMinutes)
        }

        else -> {
            stringResource(R.string.duration_less_than_a_minute)
        }
    }
}
