package com.mochi.feature.overlay

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.mochi.core.data.FloatingMochiController
import com.mochi.core.model.Nudge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayServiceController
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val overlay: FloatingMochiOverlay,
    ) : FloatingMochiController {
        private val serviceIntent get() = Intent(context, DoomscrollWatchService::class.java)

        override fun start() {
            try {
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (error: IllegalStateException) {
                // cant start a foreground service from the background on Android 12+
                Log.w(TAG, "Could not start the watch service right now", error)
            }
        }

        override fun stop() {
            context.stopService(serviceIntent)
            overlay.dismiss()
        }

        override fun preview(nudge: Nudge): Boolean = overlay.show(nudge)

        private companion object {
            const val TAG = "OverlayServiceController"
        }
    }
