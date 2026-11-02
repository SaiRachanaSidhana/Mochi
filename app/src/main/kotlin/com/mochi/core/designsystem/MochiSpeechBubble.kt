package com.mochi.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MochiSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(2.dp, MochiInk),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MochiInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        Canvas(modifier = Modifier.size(width = 22.dp, height = 12.dp).offset(y = (-2).dp)) {
            val tail =
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(size.width, 0f)
                }
            drawPath(tail, Color.White, style = Fill)
            drawPath(
                tail,
                MochiInk,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            // covers the border where the tail joins
            drawLine(
                Color.White,
                Offset(2.dp.toPx(), 0f),
                Offset(size.width - 2.dp.toPx(), 0f),
                strokeWidth = 3.dp.toPx(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MochiSpeechBubblePreview() {
    MochiTheme {
        MochiSpeechBubble(text = "it's 11 PM… we need to sleep 😴")
    }
}
