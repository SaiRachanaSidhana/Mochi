package com.mochi.core.rules

import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.MochiMessage
import com.mochi.core.model.MochiMessages
import com.mochi.core.model.Nudge
import org.junit.Test
import kotlin.random.Random

class MessagePickerTest {
    private val pool = listOf("a", "b", "c").map { MochiMessage(it, "text $it") }

    @Test
    fun `never picks a line that was already used while others remain`() {
        repeat(50) { seed ->
            val picked = MessagePicker.pick(pool, used = setOf("a", "b"), random = Random(seed))
            assertThat(picked.id).isEqualTo("c")
        }
    }

    @Test
    fun `falls back to the whole pool once everything was used`() {
        val picked = MessagePicker.pick(pool, used = setOf("a", "b", "c"), random = Random(1))

        assertThat(pool).contains(picked)
    }

    @Test
    fun `exhausted only when every line of the pool was used`() {
        assertThat(MessagePicker.exhausted(pool, setOf("a", "b"))).isFalse()
        assertThat(MessagePicker.exhausted(pool, setOf("a", "b", "c", "other"))).isTrue()
    }

    @Test
    fun `every visit of every nudge has several uniquely identified lines`() {
        val ids = mutableSetOf<String>()
        for (nudge in listOf(Nudge.DoomscrollHour, Nudge.Bedtime)) {
            for (visit in 1..MochiMessages.VISITS) {
                val lines = MochiMessages.pool(nudge, visit)
                assertThat(lines.size).isAtLeast(3)
                lines.forEach { assertThat(ids.add(it.id)).isTrue() }
            }
        }
    }
}
