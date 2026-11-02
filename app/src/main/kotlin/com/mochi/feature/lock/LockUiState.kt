package com.mochi.feature.lock

data class LockUiState(
    val step: Int = 1,
    val total: Int = 1,
    val mochiSays: String = "",
    val answer: String = "",
    val unlocked: Boolean = false,
)
