package com.mochi.feature.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.annotation.MainThread
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mochi.core.data.permissions.PermissionChecker
import com.mochi.core.designsystem.MochiTheme
import com.mochi.core.model.Nudge
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class FloatingMochiOverlay
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val permissionChecker: PermissionChecker,
    ) : NudgePresenter {
        private val windowManager: WindowManager = checkNotNull(context.getSystemService(WindowManager::class.java))
        private var activeSession: OverlaySession? = null

        override val isShowing: Boolean get() = activeSession != null

        @MainThread
        override fun show(
            nudge: Nudge,
            message: String,
        ): Boolean {
            if (activeSession != null || !permissionChecker.check().overlay) return false
            val session = OverlaySession(nudge, message)
            activeSession = session
            return try {
                session.start()
                true
            } catch (error: WindowManager.BadTokenException) {
                Log.w(TAG, "Could not add the Mochi overlay window", error)
                session.dismiss()
                false
            } catch (error: SecurityException) {
                Log.w(TAG, "Overlay permission was revoked while showing Mochi", error)
                session.dismiss()
                false
            }
        }

        @MainThread
        fun dismiss() {
            activeSession?.dismiss()
        }

        private fun onSessionFinished(session: OverlaySession) {
            if (activeSession === session) activeSession = null
        }

        private inner class OverlaySession(
            nudge: Nudge,
            message: String,
        ) {
            private val handler = Handler(Looper.getMainLooper())
            private val owner = OverlayLifecycleOwner()
            private val animationState = OverlayAnimationState()
            private val metrics = context.resources.displayMetrics
            private val contentWidthPx = with(Density(metrics.density)) { OverlayContentWidth.toPx().roundToInt() }
            private val screenWidthPx = metrics.widthPixels

            private val layoutParams =
                WindowManager
                    .LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        // lets the window sit partly off-screen
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT,
                    ).apply {
                        gravity = Gravity.BOTTOM or Gravity.START
                        x = -contentWidthPx
                        y = with(Density(metrics.density)) { BOTTOM_MARGIN.toPx().roundToInt() }
                        title = "MochiOverlay"
                    }

            private val view =
                ComposeView(context).apply {
                    setViewTreeLifecycleOwner(owner)
                    setViewTreeViewModelStoreOwner(owner)
                    setViewTreeSavedStateRegistryOwner(owner)
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    setContent {
                        MochiTheme {
                            FloatingMochiContent(nudge = nudge, message = message, state = animationState)
                        }
                    }
                }

            private var walkAnimator: ValueAnimator? = null
            private var attached = false
            private var disposed = false

            fun start() {
                owner.start()
                windowManager.addView(view, layoutParams)
                attached = true
                walkIn()
            }

            fun dismiss() {
                if (disposed) return
                disposed = true
                handler.removeCallbacksAndMessages(null)
                walkAnimator?.let {
                    it.removeAllListeners()
                    it.removeAllUpdateListeners()
                    it.cancel()
                }
                walkAnimator = null
                if (attached) {
                    try {
                        windowManager.removeViewImmediate(view)
                    } catch (error: IllegalArgumentException) {
                        Log.w(TAG, "Overlay view was already removed", error)
                    }
                    attached = false
                }
                view.disposeComposition()
                owner.destroy()
                onSessionFinished(this)
            }

            private fun walkIn() {
                val stopX = (screenWidthPx - contentWidthPx) / 2
                walk(from = -contentWidthPx, to = stopX) {
                    animationState.walking = false
                    animationState.bubbleVisible = true
                    handler.postDelayed({ walkOut(stopX) }, HOLD_MILLIS)
                }
            }

            private fun walkOut(fromX: Int) {
                animationState.bubbleVisible = false
                animationState.walking = true
                walk(from = fromX, to = screenWidthPx) { dismiss() }
            }

            private fun walk(
                from: Int,
                to: Int,
                onDone: () -> Unit,
            ) {
                walkAnimator =
                    ValueAnimator.ofInt(from, to).apply {
                        duration = WALK_MILLIS
                        interpolator = LinearInterpolator()
                        addUpdateListener { moveTo(it.animatedValue as Int) }
                        doOnStart { animationState.walking = true }
                        doOnEnd { onDone() }
                        start()
                    }
            }

            private fun moveTo(x: Int) {
                if (!attached) return
                layoutParams.x = x
                try {
                    windowManager.updateViewLayout(view, layoutParams)
                } catch (error: IllegalArgumentException) {
                    Log.w(TAG, "Overlay view is gone; stopping the walk", error)
                    walkAnimator?.cancel()
                }
            }
        }

        private companion object {
            const val TAG = "FloatingMochiOverlay"
            const val WALK_MILLIS = 1_500L
            const val HOLD_MILLIS = 4_000L
            val BOTTOM_MARGIN = 112.dp
        }
    }
