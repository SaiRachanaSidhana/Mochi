package com.mochi.feature.home

import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.Nudge
import com.mochi.core.testing.FakeFloatingMochiController
import com.mochi.core.testing.FakeSessionTracker
import com.mochi.core.testing.FakeSettingsRepository
import com.mochi.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()
    private val tracker = FakeSessionTracker()
    private val controller = FakeFloatingMochiController()

    private fun createViewModel() = HomeViewModel(settings, tracker, controller)

    @Test
    fun `state starts as loading with the switch off`() {
        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value).isEqualTo(HomeUiState())
        assertThat(viewModel.uiState.value.isLoading).isTrue()
    }

    @Test
    fun `state shows today's usage and the current session`() =
        runTest(mainDispatcherRule.dispatcher) {
            tracker.todayUsage = Duration.ofMinutes(83)
            tracker.session = Duration.ofMinutes(42)
            val viewModel = createViewModel()

            backgroundScope.launch { viewModel.uiState.collect {} }

            assertThat(viewModel.uiState.value)
                .isEqualTo(
                    HomeUiState(
                        floatingMochiEnabled = false,
                        todayUsage = Duration.ofMinutes(83),
                        currentSession = Duration.ofMinutes(42),
                        isLoading = false,
                    ),
                )
        }

    @Test
    fun `current session is null when Instagram is not in the foreground`() =
        runTest(mainDispatcherRule.dispatcher) {
            tracker.session = null
            val viewModel = createViewModel()

            backgroundScope.launch { viewModel.uiState.collect {} }

            assertThat(viewModel.uiState.value.currentSession).isNull()
            assertThat(viewModel.uiState.value.isLoading).isFalse()
        }

    @Test
    fun `usage is refreshed every minute while the screen is observed`() =
        runTest(mainDispatcherRule.dispatcher) {
            tracker.session = Duration.ofMinutes(10)
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            assertThat(viewModel.uiState.value.currentSession).isEqualTo(Duration.ofMinutes(10))

            tracker.session = Duration.ofMinutes(11)
            advanceTimeBy(HomeViewModel.REFRESH_INTERVAL_MILLIS - 1)
            runCurrent()
            assertThat(viewModel.uiState.value.currentSession).isEqualTo(Duration.ofMinutes(10))

            advanceTimeBy(2)
            runCurrent()
            assertThat(viewModel.uiState.value.currentSession).isEqualTo(Duration.ofMinutes(11))
        }

    @Test
    fun `switching on persists the choice and starts the service`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onFloatingMochiToggled(true)

            assertThat(settings.isEnabled).isTrue()
            assertThat(controller.startCount).isEqualTo(1)
            assertThat(viewModel.uiState.value.floatingMochiEnabled).isTrue()
        }

    @Test
    fun `switching off persists the choice and stops the service`() =
        runTest(mainDispatcherRule.dispatcher) {
            settings.setFloatingMochiEnabled(true)
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onFloatingMochiToggled(false)

            assertThat(settings.isEnabled).isFalse()
            assertThat(controller.stopCount).isEqualTo(1)
            assertThat(viewModel.uiState.value.floatingMochiEnabled).isFalse()
        }

    @Test
    fun `the service is revived on open if the feature is already on`() {
        runTest(mainDispatcherRule.dispatcher) {
            settings.setFloatingMochiEnabled(true)

            createViewModel()

            assertThat(controller.startCount).isEqualTo(1)
        }
    }

    @Test
    fun `the service is not started on open if the feature is off`() {
        createViewModel()

        assertThat(controller.startCount).isEqualTo(0)
    }

    @Test
    fun `preview alternates between the doomscroll and the bedtime Mochi`() {
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()
        viewModel.onPreviewClicked()
        viewModel.onPreviewClicked()

        assertThat(
            controller.previews,
        ).containsExactly(Nudge.DoomscrollHour, Nudge.Bedtime, Nudge.DoomscrollHour).inOrder()
    }

    @Test
    fun `a refused preview is retried with the same nudge`() {
        controller.previewResult = false
        val viewModel = createViewModel()

        viewModel.onPreviewClicked()
        viewModel.onPreviewClicked()

        assertThat(controller.previews).containsExactly(Nudge.DoomscrollHour, Nudge.DoomscrollHour).inOrder()
    }
}
