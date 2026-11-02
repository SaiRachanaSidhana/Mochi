package com.mochi.core.model

sealed class Nudge(
    val message: String,
) {
    data object DoomscrollHour : Nudge("You've doomscrolled for an hour 😵‍💫")

    data object Bedtime : Nudge("it's 11 PM… we need to sleep 😴")
}
