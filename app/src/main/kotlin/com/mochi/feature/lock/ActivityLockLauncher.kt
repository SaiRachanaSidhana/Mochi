package com.mochi.feature.lock

import android.content.Context
import android.content.Intent
import android.util.Log
import com.mochi.core.data.permissions.PermissionChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityLockLauncher
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val permissionChecker: PermissionChecker,
    ) : LockLauncher {
        override fun show(): Boolean {
            if (!permissionChecker.check().overlay) return false
            val intent =
                Intent(context, LockActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP,
                )
            return try {
                context.startActivity(intent)
                true
            } catch (error: SecurityException) {
                Log.w(TAG, "Could not open the lock screen", error)
                false
            }
        }

        private companion object {
            const val TAG = "ActivityLockLauncher"
        }
    }
