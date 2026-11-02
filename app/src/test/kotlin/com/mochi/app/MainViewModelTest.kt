package com.mochi.app

import com.google.common.truth.Truth.assertThat
import com.mochi.core.data.permissions.PermissionState
import com.mochi.core.testing.FakePermissionChecker
import org.junit.Test

class MainViewModelTest {
    private fun state(
        usage: Boolean,
        overlay: Boolean,
    ) = PermissionState(usage, overlay, notifications = false, notificationPermissionApplicable = true)

    @Test
    fun `onboarding is needed until both required permissions are granted`() {
        val checker = FakePermissionChecker(state(usage = true, overlay = false))
        val viewModel = MainViewModel(checker)
        assertThat(viewModel.requiredPermissionsGranted.value).isFalse()

        checker.state = state(usage = true, overlay = true)
        viewModel.refreshPermissions()

        assertThat(viewModel.requiredPermissionsGranted.value).isTrue()
    }

    @Test
    fun `a missing notification permission does not block the app`() {
        val viewModel = MainViewModel(FakePermissionChecker(state(usage = true, overlay = true)))

        assertThat(viewModel.requiredPermissionsGranted.value).isTrue()
    }

    @Test
    fun `revoking a permission sends the user back to onboarding`() {
        val checker = FakePermissionChecker(state(usage = true, overlay = true))
        val viewModel = MainViewModel(checker)

        checker.state = state(usage = false, overlay = true)
        viewModel.refreshPermissions()

        assertThat(viewModel.requiredPermissionsGranted.value).isFalse()
    }
}
