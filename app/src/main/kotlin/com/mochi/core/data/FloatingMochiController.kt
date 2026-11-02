package com.mochi.core.data

import com.mochi.core.model.Nudge

interface FloatingMochiController {
    fun start()

    fun stop()

    fun preview(nudge: Nudge): Boolean
}
