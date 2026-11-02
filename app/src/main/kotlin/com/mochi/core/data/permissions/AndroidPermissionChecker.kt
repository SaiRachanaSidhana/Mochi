package com.mochi.core.data.permissions

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidPermissionChecker
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : PermissionChecker {
        override fun check(): PermissionState =
            PermissionState(
                usageAccess = hasUsageAccess(),
                overlay = Settings.canDrawOverlays(context),
                notifications = NotificationManagerCompat.from(context).areNotificationsEnabled(),
                notificationPermissionApplicable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
            )

        // the non-deprecated version needs a newer SDK than minSdk 29
        @Suppress("DEPRECATION")
        private fun hasUsageAccess(): Boolean {
            val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
            val mode =
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName,
                )
            return if (mode == AppOpsManager.MODE_DEFAULT) {
                context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) ==
                    PackageManager.PERMISSION_GRANTED
            } else {
                mode == AppOpsManager.MODE_ALLOWED
            }
        }
    }
