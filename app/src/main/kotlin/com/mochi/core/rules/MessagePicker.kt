package com.mochi.core.rules

import com.mochi.core.model.MochiMessage
import kotlin.random.Random

object MessagePicker {
    fun pick(
        pool: List<MochiMessage>,
        used: Set<String>,
        random: Random = Random.Default,
    ): MochiMessage {
        require(pool.isNotEmpty()) { "The message pool must not be empty" }
        val fresh = pool.filter { it.id !in used }
        return (fresh.ifEmpty { pool }).random(random)
    }

    fun exhausted(
        pool: List<MochiMessage>,
        used: Set<String>,
    ): Boolean = pool.isNotEmpty() && pool.all { it.id in used }
}
