package com.mochi.feature.overlay

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mochi.core.designsystem.Mochi
import com.mochi.core.designsystem.MochiExpression
import com.mochi.core.designsystem.MochiSpeechBubble
import com.mochi.core.model.Nudge

internal val OverlayContentWidth: Dp = 240.dp

private val MochiSize = 96.dp
private val BubbleTransformOrigin = TransformOrigin(0.5f, 1f)
private const val BUBBLE_FADE_MILLIS = 250
private const val HIDDEN_BUBBLE_SCALE = 0.85f

internal class OverlayAnimationState {
    var walking by mutableStateOf(true)
    var bubbleVisible by mutableStateOf(false)
}

internal fun Nudge.expression(): MochiExpression =
    when (this) {
        Nudge.DoomscrollHour -> MochiExpression.Dizzy
        Nudge.Bedtime -> MochiExpression.Sleepy
    }

@Composable
internal fun FloatingMochiContent(
    nudge: Nudge,
    message: String,
    state: OverlayAnimationState,
    modifier: Modifier = Modifier,
) {
    val bubbleAlpha by animateFloatAsState(
        if (state.bubbleVisible) 1f else 0f,
        tween(durationMillis = BUBBLE_FADE_MILLIS),
        label = "bubbleAlpha",
    )
    val bubbleScale by animateFloatAsState(
        if (state.bubbleVisible) 1f else HIDDEN_BUBBLE_SCALE,
        tween(durationMillis = BUBBLE_FADE_MILLIS),
        label = "bubbleScale",
    )

    Column(
        modifier = modifier.width(OverlayContentWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .graphicsLayer {
                        alpha = bubbleAlpha
                        scaleX = bubbleScale
                        scaleY = bubbleScale
                        transformOrigin = BubbleTransformOrigin
                    }.semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            MochiSpeechBubble(text = message)
        }
        Spacer(Modifier.height(4.dp))
        Mochi(
            expression = nudge.expression(),
            walking = state.walking,
            size = MochiSize,
            decorative = true,
        )
    }
}
