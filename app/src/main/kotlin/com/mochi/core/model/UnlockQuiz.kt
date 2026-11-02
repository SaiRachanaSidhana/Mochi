package com.mochi.core.model

import kotlin.random.Random

data class UnlockQuestion(
    val id: String,
    val prompt: String,
    val reaction: String,
)

object UnlockQuiz {
    const val QUESTIONS_PER_UNLOCK = 3

    const val MIN_WORDS = 3

    const val TOO_SHORT_REPLY = "That's not an answer, that's a sneeze. Try a real sentence."

    fun isAcceptable(answer: String): Boolean = answer.trim().split(WHITESPACE).count { it.isNotEmpty() } >= MIN_WORDS

    fun pick(random: Random = Random.Default): List<UnlockQuestion> =
        QUESTIONS.shuffled(random).take(QUESTIONS_PER_UNLOCK)

    private val WHITESPACE = Regex("\\s+")

    internal val QUESTIONS =
        listOf(
            UnlockQuestion(
                "why",
                "Oh, back already? Tell me, what is so urgent on Instagram that it could not wait?",
                "Riveting. I'm sure the world would have ended without it.",
            ),
            UnlockQuestion(
                "lastpost",
                "Name one thing you saw in the last hour that you will remember tomorrow.",
                "Hm. Impressive recall for something you will forget by dinner.",
            ),
            UnlockQuestion(
                "instead",
                "What could you have done with the last hour instead? Be honest, I'm a blob, I can't judge. Much.",
                "Noted. The hour is gone, but the guilt is free.",
            ),
            UnlockQuestion(
                "feeling",
                "How do you feel right now, after all that scrolling? Be specific.",
                "Thanks for sharing. I'll pretend I'm surprised.",
            ),
            UnlockQuestion(
                "promise",
                "What will you do after this, besides scrolling again?",
                "Sure you will. I'll be watching. Adorably.",
            ),
            UnlockQuestion(
                "water",
                "When did you last drink water? Or look at something farther than 30 cm away?",
                "Wow. Your body sends its regards.",
            ),
            UnlockQuestion(
                "reason",
                "Convince me, in a sentence or two, that you deserve Instagram back.",
                "Persuasive. In the way a toddler asking for candy is persuasive.",
            ),
            UnlockQuestion(
                "limit",
                "How long will you stay this time? Give me a real number and a reason.",
                "I'll hold you to that. Gently. With sarcasm.",
            ),
        )
}
