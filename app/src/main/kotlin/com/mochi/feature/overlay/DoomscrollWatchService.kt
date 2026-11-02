package com.mochi.feature.overlay

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.mochi.app.R
import com.mochi.core.data.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class DoomscrollWatchService : LifecycleService() {
    @Inject
    lateinit var watcher: NudgeWatcher

    @Inject
    lateinit var overlay: FloatingMochiOverlay

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val screenOn = MutableStateFlow(true)

    private val screenReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> screenOn.value = true
                    Intent.ACTION_SCREEN_OFF -> screenOn.value = false
                }
            }
        }

    override fun onCreate() {
        super.onCreate()
        promoteToForeground()
        screenOn.value = getSystemService(PowerManager::class.java)?.isInteractive ?: true
        ContextCompat.registerReceiver(
            this,
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        lifecycleScope.launch { watchLoop() }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(screenReceiver)
        overlay.dismiss()
        super.onDestroy()
    }

    private suspend fun watchLoop() {
        // the user may have turned it off before the restart
        if (!settingsRepository.floatingMochiEnabled.first()) {
            stopSelf()
            return
        }
        screenOn.collectLatest { isOn ->
            if (!isOn) return@collectLatest
            while (true) {
                currentCoroutineContext().ensureActive()
                val inForeground = safeTick()
                delay(
                    when {
                        inForeground && watcher.isLocked() -> LOCKED_POLL
                        inForeground -> FOREGROUND_POLL
                        else -> BACKGROUND_POLL
                    },
                )
            }
        }
    }

    private suspend fun safeTick(): Boolean =
        try {
            watcher.tick()
        } catch (error: SecurityException) {
            Log.w(TAG, "Usage access is not available", error)
            false
        } catch (error: IllegalStateException) {
            Log.w(TAG, "Watch check failed", error)
            false
        }

    private fun promoteToForeground() {
        val manager = NotificationManagerCompat.from(this)
        manager.createNotificationChannel(
            NotificationChannelCompat
                .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(getString(R.string.watch_channel_name))
                .setDescription(getString(R.string.watch_channel_description))
                .setShowBadge(false)
                .build(),
        )

        val openApp =
            packageManager.getLaunchIntentForPackage(packageName)?.let {
                PendingIntent.getActivity(
                    this,
                    0,
                    it,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            }
        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.watch_notification_title))
                .setContentIntent(openApp)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()

        val type =
            if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.UPSIDE_DOWN_CAKE
            ) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    private companion object {
        const val TAG = "DoomscrollWatchService"
        const val CHANNEL_ID = "mochi_watch"
        const val NOTIFICATION_ID = 1
        val LOCKED_POLL = 2.seconds
        val FOREGROUND_POLL = 30.seconds
        val BACKGROUND_POLL = 60.seconds
    }
}
