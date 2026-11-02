package com.mochi.feature.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mochi.core.data.FloatingMochiController
import com.mochi.core.data.settings.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface BootEntryPoint {
        fun settingsRepository(): SettingsRepository

        fun controller(): FloatingMochiController
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, BootEntryPoint::class.java)
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                if (entryPoint.settingsRepository().floatingMochiEnabled.first()) entryPoint.controller().start()
            } finally {
                pending.finish()
            }
        }
    }
}
