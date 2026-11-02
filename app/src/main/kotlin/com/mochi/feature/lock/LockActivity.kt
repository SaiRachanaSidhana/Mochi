package com.mochi.feature.lock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mochi.core.designsystem.MochiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class LockActivity : ComponentActivity() {
    private val viewModel: LockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = leave()
            },
        )
        setContent {
            MochiTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(state.unlocked) {
                    if (state.unlocked) {
                        delay(UNLOCKED_CLOSE_DELAY_MILLIS)
                        finish()
                    }
                }
                LockScreen(
                    state = state,
                    onAnswerChanged = viewModel::onAnswerChanged,
                    onSubmit = viewModel::onSubmit,
                    onLeave = ::leave,
                )
            }
        }
    }

    private fun leave() {
        startActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        finish()
    }

    private companion object {
        const val UNLOCKED_CLOSE_DELAY_MILLIS = 2_000L
    }
}
