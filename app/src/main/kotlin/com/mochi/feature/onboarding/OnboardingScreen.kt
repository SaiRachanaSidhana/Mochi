package com.mochi.feature.onboarding

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mochi.app.R
import com.mochi.core.data.permissions.PermissionState
import com.mochi.core.designsystem.Mochi
import com.mochi.core.designsystem.MochiExpression
import com.mochi.core.designsystem.MochiTheme

@SuppressLint("InlinedApi")
@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onFinished()
    }

    val notificationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.onNotificationsResolved()
        }

    OnboardingScreen(
        state = state,
        onGrantClick = { step ->
            when (step) {
                PermissionStep.Notifications -> notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else -> context.openSettingsFor(step)
            }
        },
        onSkipNotifications = viewModel::onNotificationsResolved,
        modifier = modifier,
    )
}

@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    onGrantClick: (PermissionStep) -> Unit,
    onSkipNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = state.currentStep ?: return
    val content = step.content()

    Scaffold(modifier = modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.onboarding_step_indicator,
                        state.steps.indexOf(step) + 1,
                        state.steps.size,
                    ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(8.dp))
            Mochi(expression = content.expression, size = 180.dp)
            Text(
                text = stringResource(content.title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(content.body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(8.dp))
            Button(
                onClick = { onGrantClick(step) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text(stringResource(content.button), style = MaterialTheme.typography.labelLarge)
            }
            if (step == PermissionStep.Notifications) {
                TextButton(
                    onClick = onSkipNotifications,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.onboarding_notifications_skip))
                }
            }
        }
    }
}

private class StepContent(
    val expression: MochiExpression,
    val title: Int,
    val body: Int,
    val button: Int,
)

private fun PermissionStep.content(): StepContent =
    when (this) {
        PermissionStep.UsageAccess -> {
            StepContent(
                MochiExpression.Happy,
                R.string.onboarding_usage_title,
                R.string.onboarding_usage_body,
                R.string.onboarding_usage_button,
            )
        }

        PermissionStep.Overlay -> {
            StepContent(
                MochiExpression.Happy,
                R.string.onboarding_overlay_title,
                R.string.onboarding_overlay_body,
                R.string.onboarding_overlay_button,
            )
        }

        PermissionStep.Notifications -> {
            StepContent(
                MochiExpression.Sleepy,
                R.string.onboarding_notifications_title,
                R.string.onboarding_notifications_body,
                R.string.onboarding_notifications_button,
            )
        }
    }

@Preview(showBackground = true)
@Composable
private fun OnboardingPreview() {
    MochiTheme {
        OnboardingScreen(
            state =
                OnboardingUiState(
                    PermissionState(
                        usageAccess = false,
                        overlay = false,
                        notifications = false,
                        notificationPermissionApplicable = true,
                    ),
                ),
            onGrantClick = {},
            onSkipNotifications = {},
        )
    }
}
