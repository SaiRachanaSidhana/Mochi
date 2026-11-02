package com.mochi.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mochi.core.data.permissions.AndroidPermissionChecker
import com.mochi.core.data.permissions.PermissionChecker
import com.mochi.core.data.settings.DataStoreSettingsRepository
import com.mochi.core.data.settings.SettingsRepository
import com.mochi.core.data.usage.InstagramSessionTracker
import com.mochi.core.data.usage.SessionTracker
import com.mochi.core.data.usage.UsageEventSource
import com.mochi.core.data.usage.UsageStatsEventSource
import com.mochi.core.model.WatchedApps
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindUsageEventSource(impl: UsageStatsEventSource): UsageEventSource

    @Binds
    abstract fun bindSessionTracker(impl: InstagramSessionTracker): SessionTracker

    @Binds
    abstract fun bindPermissionChecker(impl: AndroidPermissionChecker): PermissionChecker
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidersModule {
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            produceFile = { context.preferencesDataStoreFile("mochi_settings") },
        )

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    fun provideWatchedApps(): WatchedApps = WatchedApps.DEFAULT

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
