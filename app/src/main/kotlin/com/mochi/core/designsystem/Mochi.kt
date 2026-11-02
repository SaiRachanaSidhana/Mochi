@file:Suppress("MagicNumber")

package com.mochi.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mochi.app.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private const val TWO_PI = (2 * PI).toFloat()

@Composable
fun Mochi(
    modifier: Modifier = Modifier,
    expression: MochiExpression = MochiExpression.Happy,
    walking: Boolean = false,
    size: Dp = 160.dp,
    decorative: Boolean = false,
) {
    val transition = rememberInfiniteTransition(label = "mochi")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = if (walking) WALK_STEP_MILLIS else IDLE_BREATH_MILLIS,
                        easing = LinearEasing,
                    ),
                repeatMode = RepeatMode.Restart,
            ),
        label = "mochiPhase",
    )

    val description =
        stringResource(
            when (expression) {
                MochiExpression.Happy -> R.string.mochi_cd_happy
                MochiExpression.Dizzy -> R.string.mochi_cd_dizzy
                MochiExpression.Sleepy -> R.string.mochi_cd_sleepy
            },
        )
    val semanticsModifier =
        if (decorative) {
            Modifier
        } else {
            Modifier.semantics {
                contentDescription = description
                role = Role.Image
            }
        }

    Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
        drawMochi(expression = expression, walking = walking, phase = phase)
    }
}

private const val WALK_STEP_MILLIS = 650
private const val IDLE_BREATH_MILLIS = 2400

private fun DrawScope.drawMochi(
    expression: MochiExpression,
    walking: Boolean,
    phase: Float,
) {
    val s = size.minDimension
    val wave = sin(phase * TWO_PI)
    val step = abs(sin(phase * PI.toFloat())) // 0 on the ground, 1 at the top of a hop

    val hop = if (walking) step * 0.07f * s else 0f
    val scaleX = if (walking) 1.05f - 0.09f * step else 1f - 0.02f * wave
    val scaleY = if (walking) 0.94f + 0.11f * step else 1f + 0.03f * wave
    val tilt = if (walking) wave * 4f else 0f
    val pivot = Offset(0.5f * s, 0.92f * s)

    withTransform({
        translate(top = -hop)
        rotate(tilt, pivot)
        scale(scaleX, scaleY, pivot)
    }) {
        drawFeet(s, walking, wave)
        drawBody(s)
        drawCheeks(s)
        drawFace(s, expression, phase)
        drawLeaf(s, wave)
        if (expression == MochiExpression.Sleepy) drawZzz(s, phase)
    }
}

private fun DrawScope.outline(s: Float) = Stroke(width = 0.035f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun DrawScope.drawFeet(
    s: Float,
    walking: Boolean,
    wave: Float,
) {
    val leftLift = if (walking) max(0f, wave) * 0.05f * s else 0f
    val rightLift = if (walking) max(0f, -wave) * 0.05f * s else 0f
    listOf(0.33f to leftLift, 0.67f to rightLift).forEach { (cx, lift) ->
        val topLeft = Offset((cx - 0.08f) * s, 0.90f * s - lift)
        val footSize = Size(0.16f * s, 0.08f * s)
        drawOval(MochiGreen, topLeft, footSize, style = Fill)
        drawOval(MochiInk, topLeft, footSize, style = outline(s))
    }
}

private fun DrawScope.drawBody(s: Float) {
    val body =
        Path().apply {
            moveTo(0.50f * s, 0.14f * s)
            cubicTo(0.78f * s, 0.14f * s, 0.94f * s, 0.40f * s, 0.94f * s, 0.66f * s)
            cubicTo(0.94f * s, 0.84f * s, 0.80f * s, 0.93f * s, 0.50f * s, 0.93f * s)
            cubicTo(0.20f * s, 0.93f * s, 0.06f * s, 0.84f * s, 0.06f * s, 0.66f * s)
            cubicTo(0.06f * s, 0.40f * s, 0.22f * s, 0.14f * s, 0.50f * s, 0.14f * s)
            close()
        }
    drawPath(body, MochiGreen, style = Fill)
    rotate(-25f, pivot = Offset(0.30f * s, 0.36f * s)) {
        drawOval(
            color = Color.White.copy(alpha = 0.45f),
            topLeft = Offset(0.22f * s, 0.31f * s),
            size = Size(0.14f * s, 0.08f * s),
        )
    }
    drawPath(body, MochiInk, style = outline(s))
}

private fun DrawScope.drawCheeks(s: Float) {
    listOf(0.22f, 0.78f).forEach { cx ->
        drawOval(
            color = MochiCheek.copy(alpha = 0.9f),
            topLeft = Offset((cx - 0.07f) * s, 0.60f * s),
            size = Size(0.14f * s, 0.085f * s),
        )
    }
}

private fun DrawScope.drawFace(
    s: Float,
    expression: MochiExpression,
    phase: Float,
) {
    val eyeY = 0.50f * s
    val eyeXs = listOf(0.35f * s, 0.65f * s)
    val line = Stroke(width = 0.026f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (expression) {
        MochiExpression.Happy -> {
            eyeXs.forEach { x ->
                drawOval(MochiInk, Offset(x - 0.035f * s, eyeY - 0.05f * s), Size(0.07f * s, 0.10f * s))
                drawCircle(Color.White, 0.014f * s, Offset(x + 0.008f * s, eyeY - 0.025f * s))
            }
        }

        MochiExpression.Sleepy -> {
            eyeXs.forEach { x ->
                val lid =
                    Path().apply {
                        moveTo(x - 0.05f * s, eyeY)
                        quadraticTo(x, eyeY + 0.055f * s, x + 0.05f * s, eyeY)
                    }
                drawPath(lid, MochiInk, style = line)
            }
        }

        MochiExpression.Dizzy -> {
            eyeXs.forEachIndexed { index, x ->
                val direction = if (index == 0) 1f else -1f
                rotate(phase * 360f * direction, pivot = Offset(x, eyeY)) {
                    drawPath(
                        spiral(Offset(x, eyeY), 0.055f * s),
                        MochiInk,
                        style = Stroke(0.02f * s, cap = StrokeCap.Round),
                    )
                }
            }
        }
    }

    val mouth =
        Path().apply {
            when (expression) {
                MochiExpression.Happy -> {
                    moveTo(0.43f * s, 0.66f * s)
                    quadraticTo(0.50f * s, 0.74f * s, 0.57f * s, 0.66f * s)
                }

                MochiExpression.Dizzy -> {
                    moveTo(0.40f * s, 0.71f * s)
                    cubicTo(0.44f * s, 0.67f * s, 0.47f * s, 0.75f * s, 0.50f * s, 0.71f * s)
                    cubicTo(0.53f * s, 0.67f * s, 0.56f * s, 0.75f * s, 0.60f * s, 0.71f * s)
                }

                MochiExpression.Sleepy -> {
                    addOval(
                        androidx.compose.ui.geometry
                            .Rect(Offset(0.5f * s, 0.70f * s), 0.024f * s),
                    )
                }
            }
        }
    drawPath(mouth, MochiInk, style = line)
}

private fun spiral(
    center: Offset,
    radius: Float,
): Path {
    val steps = 44
    val turns = 2.2f
    return Path().apply {
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val angle = t * turns * TWO_PI
            val r = t * radius
            val point = Offset(center.x + r * cos(angle), center.y + r * sin(angle))
            if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
    }
}

private fun DrawScope.drawLeaf(
    s: Float,
    wave: Float,
) {
    rotate(wave * 6f, pivot = Offset(0.50f * s, 0.17f * s)) {
        val leaf =
            Path().apply {
                moveTo(0.50f * s, 0.17f * s)
                cubicTo(0.45f * s, 0.08f * s, 0.52f * s, 0.00f * s, 0.65f * s, 0.01f * s)
                cubicTo(0.67f * s, 0.12f * s, 0.60f * s, 0.20f * s, 0.50f * s, 0.17f * s)
                close()
            }
        drawPath(leaf, MochiLeaf, style = Fill)
        drawPath(leaf, MochiInk, style = Stroke(0.025f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val vein =
            Path().apply {
                moveTo(0.52f * s, 0.15f * s)
                quadraticTo(0.57f * s, 0.10f * s, 0.62f * s, 0.05f * s)
            }
        drawPath(vein, MochiInk.copy(alpha = 0.6f), style = Stroke(0.012f * s, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawZzz(
    s: Float,
    phase: Float,
) {
    listOf(0f to 0.10f, 0.5f to 0.07f).forEach { (offset, zSize) ->
        val t = (phase + offset) % 1f
        val origin = Offset(0.80f * s + t * 0.05f * s, 0.30f * s - t * 0.16f * s)
        val z =
            Path().apply {
                moveTo(origin.x, origin.y)
                lineTo(origin.x + zSize * s, origin.y)
                lineTo(origin.x, origin.y + zSize * s)
                lineTo(origin.x + zSize * s, origin.y + zSize * s)
            }
        drawPath(
            z,
            MochiInk.copy(alpha = (1f - t).coerceIn(0f, 1f)),
            style = Stroke(0.022f * s, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
