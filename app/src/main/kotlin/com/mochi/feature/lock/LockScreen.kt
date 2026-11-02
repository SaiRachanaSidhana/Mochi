package com.mochi.feature.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mochi.app.R
import com.mochi.core.designsystem.Mochi
import com.mochi.core.designsystem.MochiExpression
import com.mochi.core.designsystem.MochiSpeechBubble
import com.mochi.core.designsystem.MochiTheme

@Composable
internal fun LockScreen(
    state: LockUiState,
    onAnswerChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier
                    .systemBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.lock_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.lock_step, state.step, state.total),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MochiSpeechBubble(text = state.mochiSays)
            Mochi(
                expression = if (state.unlocked) MochiExpression.Happy else MochiExpression.Dizzy,
                size = MOCHI_SIZE,
            )
            Spacer(Modifier.padding(top = 4.dp))

            OutlinedTextField(
                value = state.answer,
                onValueChange = onAnswerChanged,
                enabled = !state.unlocked,
                label = { Text(stringResource(R.string.lock_answer_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            )
            Button(
                onClick = onSubmit,
                enabled = !state.unlocked,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.lock_submit))
            }
            TextButton(onClick = onLeave, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.lock_leave))
            }
        }
    }
}

private val MOCHI_SIZE = 120.dp

@Preview(showBackground = true)
@Composable
private fun LockScreenPreview() {
    MochiTheme {
        LockScreen(
            state =
                LockUiState(
                    step = 1,
                    total = 3,
                    mochiSays = "Oh, back already? Tell me, what is so urgent on Instagram?",
                ),
            onAnswerChanged = {},
            onSubmit = {},
            onLeave = {},
        )
    }
}
