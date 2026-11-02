package com.mochi.feature.settings

import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.NudgeSettings
import com.mochi.core.testing.FakeSettingsRepository
import com.mochi.core.testing.MainDispatcherRule
import com.mochi.core.testing.MutableClock
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()

    private val clock = MutableClock(Instant.parse("2026-03-10T15:42:30Z"))

    private fun createViewModel() = SettingsViewModel(repository, clock)

    @Test
    fun `state starts with the defaults`() {
        assertThat(createViewModel().uiState.value.settings).isEqualTo(NudgeSettings.DEFAULT)
    }

    @Test
    fun `state mirrors what is stored`() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.updateSettings { it.copy(doomscrollLimit = Duration.ofMinutes(90)) }
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertThat(viewModel.uiState.value.settings.doomscrollLimit).isEqualTo(Duration.ofMinutes(90))
        }

    @Test
    fun `selecting a doomscroll limit and a cooldown updates the settings`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onDoomscrollLimitSelected(45)
            viewModel.onCooldownSelected(15)

            val settings = viewModel.uiState.value.settings
            assertThat(settings.doomscrollLimit).isEqualTo(Duration.ofMinutes(45))
            assertThat(settings.cooldown).isEqualTo(Duration.ofMinutes(15))
        }

    @Test
    fun `changing bedtime and wake time updates the settings`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onBedtimeChanged(LocalTime.of(22, 30))
            viewModel.onWakeTimeChanged(LocalTime.of(6, 45))

            val settings = viewModel.uiState.value.settings
            assertThat(settings.bedtime).isEqualTo(LocalTime.of(22, 30))
            assertThat(settings.wakeTime).isEqualTo(LocalTime.of(6, 45))
        }

    @Test
    fun `reset to defaults restores every setting`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            viewModel.onDoomscrollLimitSelected(120)
            viewModel.onBedtimeChanged(LocalTime.of(21, 0))

            viewModel.onResetToDefaults()

            assertThat(viewModel.uiState.value.settings).isEqualTo(NudgeSettings.DEFAULT)
        }

    @Test
    fun `debug doomscroll preset uses a 1 minute limit and moves bedtime away from now`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onDebugDoomscrollPreset()

            val settings = viewModel.uiState.value.settings
            assertThat(settings.doomscrollLimit).isEqualTo(Duration.ofMinutes(1))
            assertThat(settings.cooldown).isEqualTo(Duration.ofMinutes(1))
            assertThat(settings.bedtime).isEqualTo(LocalTime.of(3, 42))
            assertThat(settings.wakeTime).isEqualTo(LocalTime.of(5, 42))
        }

    @Test
    fun `debug bedtime preset starts bedtime at the current minute`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.onDebugBedtimePreset()

            val settings = viewModel.uiState.value.settings
            assertThat(settings.bedtime).isEqualTo(LocalTime.of(15, 42))
            assertThat(settings.wakeTime).isEqualTo(LocalTime.of(17, 42))
            assertThat(settings.cooldown).isEqualTo(Duration.ofMinutes(1))
        }
}
