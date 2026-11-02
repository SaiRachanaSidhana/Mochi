package com.mochi.feature.onboarding

import com.google.common.truth.Truth.assertThat
import com.mochi.core.data.permissions.PermissionState
import com.mochi.core.testing.FakePermissionChecker
import org.junit.Test

class OnboardingViewModelTest {
    private fun state(
        usage: Boolean = false,
        overlay: Boolean = false,
        notifications: Boolean = false,
        notificationsApplicable: Boolean = true,
    ) = PermissionState(usage, overlay, notifications, notificationsApplicable)

    @Test
    fun `starts at usage access when nothing is granted`() {
        val viewModel = OnboardingViewModel(FakePermissionChecker(state()))

        assertThat(viewModel.uiState.value.currentStep).isEqualTo(PermissionStep.UsageAccess)
        assertThat(viewModel.uiState.value.isComplete).isFalse()
    }

    @Test
    fun `moves on to overlay once usage access is granted and the user comes back`() {
        val checker = FakePermissionChecker(state())
        val viewModel = OnboardingViewModel(checker)

        checker.state = state(usage = true)
        viewModel.refresh()

        assertThat(viewModel.uiState.value.currentStep).isEqualTo(PermissionStep.Overlay)
    }

    @Test
    fun `asks for notifications last on Android 13 and above`() {
        val checker = FakePermissionChecker(state(usage = true, overlay = true))
        val viewModel = OnboardingViewModel(checker)

        assertThat(viewModel.uiState.value.steps)
            .containsExactly(PermissionStep.UsageAccess, PermissionStep.Overlay, PermissionStep.Notifications)
            .inOrder()
        assertThat(viewModel.uiState.value.currentStep).isEqualTo(PermissionStep.Notifications)
    }

    @Test
    fun `skipping notifications completes onboarding`() {
        val viewModel = OnboardingViewModel(FakePermissionChecker(state(usage = true, overlay = true)))

        viewModel.onNotificationsResolved()

        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun `there is no notification step before Android 13`() {
        val viewModel =
            OnboardingViewModel(
                FakePermissionChecker(state(usage = true, overlay = true, notificationsApplicable = false)),
            )

        assertThat(viewModel.uiState.value.steps)
            .containsExactly(PermissionStep.UsageAccess, PermissionStep.Overlay)
        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun `granted notifications complete onboarding without a skip`() {
        val viewModel =
            OnboardingViewModel(FakePermissionChecker(state(usage = true, overlay = true, notifications = true)))

        assertThat(viewModel.uiState.value.isComplete).isTrue()
    }

    @Test
    fun `revoking a permission later sends the user back to that step`() {
        val checker = FakePermissionChecker(state(usage = true, overlay = true, notifications = true))
        val viewModel = OnboardingViewModel(checker)

        checker.state = state(usage = true, overlay = false, notifications = true)
        viewModel.refresh()

        assertThat(viewModel.uiState.value.currentStep).isEqualTo(PermissionStep.Overlay)
    }
}
