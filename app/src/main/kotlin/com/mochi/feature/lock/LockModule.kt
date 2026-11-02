package com.mochi.feature.lock

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LockModule {
    @Binds
    abstract fun bindLockLauncher(impl: ActivityLockLauncher): LockLauncher
}
