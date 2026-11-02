package com.mochi.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

class UnlockQuizTest {
    @Test
    fun `short answers are rejected`() {
        assertThat(UnlockQuiz.isAcceptable("")).isFalse()
        assertThat(UnlockQuiz.isAcceptable("idk")).isFalse()
        assertThat(UnlockQuiz.isAcceptable("   no   thanks  ")).isFalse()
    }

    @Test
    fun `a real sentence is accepted`() {
        assertThat(UnlockQuiz.isAcceptable("I wanted to check one message")).isTrue()
        assertThat(UnlockQuiz.isAcceptable("  three  small   words ")).isTrue()
    }

    @Test
    fun `picks the configured number of different questions`() {
        repeat(20) { seed ->
            val picked = UnlockQuiz.pick(Random(seed))

            assertThat(picked).hasSize(UnlockQuiz.QUESTIONS_PER_UNLOCK)
            assertThat(picked.map { it.id }.toSet()).hasSize(UnlockQuiz.QUESTIONS_PER_UNLOCK)
        }
    }

    @Test
    fun `question ids are unique`() {
        val ids = UnlockQuiz.QUESTIONS.map { it.id }

        assertThat(ids.toSet()).hasSize(ids.size)
        assertThat(ids.size).isAtLeast(UnlockQuiz.QUESTIONS_PER_UNLOCK)
    }
}
