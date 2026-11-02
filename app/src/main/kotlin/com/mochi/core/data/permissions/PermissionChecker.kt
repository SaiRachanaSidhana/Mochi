package com.mochi.core.data.permissions

data class PermissionState(
    val usageAccess: Boolean,
    val overlay: Boolean,
    val notifications: Boolean,
    val notificationPermissionApplicable: Boolean,
) {
    val requiredGranted: Boolean get() = usageAccess && overlay
}

interface PermissionChecker {
    fun check(): PermissionState
}
