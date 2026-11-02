package com.mochi.feature.overlay

import com.mochi.core.data.FloatingMochiController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OverlayModule {
    @Binds
    abstract fun bindController(impl: OverlayServiceController): FloatingMochiController

    @Binds
    abstract fun bindPresenter(impl: FloatingMochiOverlay): NudgePresenter
}
