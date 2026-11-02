package com.mochi.core.model

data class MochiMessage(
    val id: String,
    val text: String,
)

object MochiMessages {
    const val VISITS = 5

    fun pool(
        nudge: Nudge,
        visit: Int,
    ): List<MochiMessage> {
        val tiers =
            when (nudge) {
                Nudge.DoomscrollHour -> DOOMSCROLL
                Nudge.Bedtime -> BEDTIME
            }
        val tier = visit.coerceIn(1, VISITS) - 1
        return tiers[tier].mapIndexed { index, text ->
            MochiMessage(id = "${nudge.idPrefix}-${tier + 1}-${index + 1}", text = text)
        }
    }

    private val Nudge.idPrefix: String
        get() =
            when (this) {
                Nudge.DoomscrollHour -> "doom"
                Nudge.Bedtime -> "bed"
            }

    private val DOOMSCROLL =
        listOf(
            listOf(
                "Hi! It has been an hour. Just saying 👋",
                "An hour already? Time flies when you ignore me.",
                "Quick check-in: still scrolling, I see.",
                "Hello from the other side of your feed 👀",
                "Hey. Remember me? Small, mint, judging you?",
            ),
            listOf(
                "Wow, an hour of reels. Your thumb must be so proud.",
                "Fascinating content, I assume? Worth every minute?",
                "I counted. Sixty minutes. I'm not mad, just impressed.",
                "Another post, another piece of your life gone. Cute.",
                "Do the videos ever end? Asking for your thumb.",
            ),
            listOf(
                "Oh good, you're still here. I was getting worried you'd do something useful.",
                "The algorithm says thank you. Your to-do list says nothing.",
                "Breaking news: the feed is infinite. You are not.",
                "Bold strategy, scrolling until something changes.",
                "I'd clap, but I don't have hands. Or interest in your feed.",
            ),
            listOf(
                "Your future self just sighed. Loudly.",
                "That's it. I'm moving into your screen. Rent is one closed app.",
                "Somewhere a plant you forgot to water is crying.",
                "I brought snacks for your attention span. It left already.",
                "Plot twist: nobody in that feed knows you're watching.",
            ),
            listOf(
                "Last warning, champion. Next time I lock the door.",
                "Final visit. I'm taking Instagram away now. You're welcome.",
                "I tried charm, I tried sarcasm. Now I try a lock 🔒",
                "Enough. Go touch grass. I'll hold Instagram hostage.",
                "Okay, that was five. Instagram is locked. Come talk to me.",
            ),
        )

    private val BEDTIME =
        listOf(
            listOf(
                "It's late. Gentle reminder that beds exist 🛏️",
                "Hi, it's me. The sleepy one. It's bedtime.",
                "Psst. The moon says go to sleep.",
                "Late night scroll? Cozy. But also, sleep.",
                "Just a tiny yawn to tell you it's very late 😴",
            ),
            listOf(
                "Wow, still awake. The sleep fairy filed a complaint.",
                "Bold of you to scroll when your alarm is plotting revenge.",
                "Nothing good happens in the feed after midnight. Probably.",
                "Your pillow is starting to take this personally.",
                "I'm a blob and even I know you need sleep.",
            ),
            listOf(
                "Oh, you're doing the 'just one more post' thing again. Classic.",
                "Tomorrow-you is going to be so thrilled with tonight-you.",
                "Sleep is free, you know. Still undefeated.",
                "Your eyes called. They want to retire early.",
                "Great plan: be exhausted tomorrow. Very productive.",
            ),
            listOf(
                "Your future self is already drafting a complaint letter.",
                "The dark circles will be sponsored by Instagram.",
                "I'd tuck you in, but you keep holding the phone.",
                "Even your phone battery wants you to rest.",
                "Congratulations, you're now the night shift of nothing.",
            ),
            listOf(
                "Final visit. Instagram is going to bed. You should too 🔒",
                "Okay, that's five. I'm locking Instagram. Sweet dreams.",
                "I tried sarcasm. Now I try consequences. Night night 🔒",
                "Enough. Lights out. Instagram is locked until you talk to me.",
                "Last one, I swear. Locking the app. Go sleep, please.",
            ),
        )
}
