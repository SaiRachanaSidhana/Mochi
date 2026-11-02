package com.mochi.feature.onboarding

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

internal fun Context.openSettingsFor(step: PermissionStep) {
    val action =
        when (step) {
            PermissionStep.UsageAccess -> Settings.ACTION_USAGE_ACCESS_SETTINGS
            PermissionStep.Overlay -> Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            PermissionStep.Notifications -> return
        }
    try {
        startActivity(Intent(action, "package:$packageName".toUri()))
    } catch (_: ActivityNotFoundException) {
        // some phones cant open the per-app page, use the list
        startActivity(Intent(action))
    }
}
