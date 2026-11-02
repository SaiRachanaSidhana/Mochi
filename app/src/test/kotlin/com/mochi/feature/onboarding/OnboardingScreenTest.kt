package com.mochi.feature.onboarding

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.mochi.app.R
import com.mochi.core.data.permissions.PermissionState
import com.mochi.core.designsystem.MochiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OnboardingScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun permissions(
        usage: Boolean = false,
        overlay: Boolean = false,
        notifications: Boolean = false,
    ) = PermissionState(usage, overlay, notifications, notificationPermissionApplicable = true)

    private fun show(
        state: OnboardingUiState,
        onGrantClick: (PermissionStep) -> Unit = {},
        onSkipNotifications: () -> Unit = {},
    ) {
        composeRule.setContent {
            MochiTheme {
                OnboardingScreen(state, onGrantClick, onSkipNotifications)
            }
        }
    }

    @Test
    fun `first screen explains usage access`() {
        show(OnboardingUiState(permissions()))

        composeRule.onNodeWithText(context.getString(R.string.onboarding_usage_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_usage_body)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_usage_button)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_step_indicator, 1, 3)).assertIsDisplayed()
    }

    @Test
    fun `mochi illustration has a content description`() {
        show(OnboardingUiState(permissions()))

        composeRule.onNodeWithContentDescription(context.getString(R.string.mochi_cd_happy)).assertIsDisplayed()
    }

    @Test
    fun `tapping the button asks to open the usage access settings`() {
        val clicked = mutableListOf<PermissionStep>()
        show(OnboardingUiState(permissions()), onGrantClick = { clicked += it })

        composeRule.onNodeWithText(context.getString(R.string.onboarding_usage_button)).performScrollTo().performClick()

        assertThat(clicked).containsExactly(PermissionStep.UsageAccess)
    }

    @Test
    fun `overlay screen is shown once usage access is granted`() {
        val clicked = mutableListOf<PermissionStep>()
        show(OnboardingUiState(permissions(usage = true)), onGrantClick = { clicked += it })

        composeRule.onNodeWithText(context.getString(R.string.onboarding_overlay_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_step_indicator, 2, 3)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(R.string.onboarding_overlay_button),
            ).performScrollTo()
            .performClick()

        assertThat(clicked).containsExactly(PermissionStep.Overlay)
    }

    @Test
    fun `notification screen can be skipped`() {
        var skipped = false
        show(
            OnboardingUiState(permissions(usage = true, overlay = true)),
            onSkipNotifications = { skipped = true },
        )

        composeRule.onNodeWithText(context.getString(R.string.onboarding_notifications_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.mochi_cd_sleepy)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(R.string.onboarding_notifications_skip),
            ).performScrollTo()
            .performClick()

        assertThat(skipped).isTrue()
    }

    @Test
    fun `notification screen can ask for the runtime permission`() {
        val clicked = mutableListOf<PermissionStep>()
        show(OnboardingUiState(permissions(usage = true, overlay = true)), onGrantClick = { clicked += it })

        composeRule
            .onNodeWithText(
                context.getString(R.string.onboarding_notifications_button),
            ).performScrollTo()
            .performClick()

        assertThat(clicked).containsExactly(PermissionStep.Notifications)
    }
}
