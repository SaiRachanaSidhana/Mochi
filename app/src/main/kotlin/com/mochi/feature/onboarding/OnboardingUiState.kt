package com.mochi.feature.onboarding

import com.mochi.core.data.permissions.PermissionState

enum class PermissionStep {
    UsageAccess,
    Overlay,
    Notifications,
}

data class OnboardingUiState(
    val permissions: PermissionState,
    val notificationsResolved: Boolean = false,
) {
    val steps: List<PermissionStep> =
        buildList {
            add(PermissionStep.UsageAccess)
            add(PermissionStep.Overlay)
            if (permissions.notificationPermissionApplicable) add(PermissionStep.Notifications)
        }

    val currentStep: PermissionStep? = steps.firstOrNull { !isDone(it) }

    val isComplete: Boolean get() = currentStep == null

    private fun isDone(step: PermissionStep): Boolean =
        when (step) {
            PermissionStep.UsageAccess -> permissions.usageAccess
            PermissionStep.Overlay -> permissions.overlay
            PermissionStep.Notifications -> permissions.notifications || notificationsResolved
        }
}
