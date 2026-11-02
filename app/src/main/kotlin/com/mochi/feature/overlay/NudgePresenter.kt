package com.mochi.feature.overlay

import com.mochi.core.model.Nudge

interface NudgePresenter {
    val isShowing: Boolean

    fun show(
        nudge: Nudge,
        message: String = nudge.message,
    ): Boolean
}
