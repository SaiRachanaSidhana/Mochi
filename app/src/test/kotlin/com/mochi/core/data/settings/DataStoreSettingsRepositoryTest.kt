package com.mochi.core.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.NudgeSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class DataStoreSettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: DataStoreSettingsRepository

    @Before
    fun setUp() {
        val dataStore =
            PreferenceDataStoreFactory.create(scope = scope) {
                File(temporaryFolder.root, "settings.preferences_pb")
            }
        repository = DataStoreSettingsRepository(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `defaults are 60 min limit, 11 PM bedtime, 5 AM wake and 30 min cooldown`() =
        runTest {
            val settings = repository.settings.first()

            assertThat(settings).isEqualTo(NudgeSettings.DEFAULT)
            assertThat(settings.doomscrollLimit).isEqualTo(Duration.ofMinutes(60))
            assertThat(settings.bedtime).isEqualTo(LocalTime.of(23, 0))
            assertThat(settings.wakeTime).isEqualTo(LocalTime.of(5, 0))
            assertThat(settings.cooldown).isEqualTo(Duration.ofMinutes(30))
            assertThat(repository.floatingMochiEnabled.first()).isFalse()
            assertThat(repository.lastNudgeAt.first()).isNull()
        }

    @Test
    fun `updated settings are persisted and emitted`() =
        runTest {
            repository.settings.test {
                assertThat(awaitItem()).isEqualTo(NudgeSettings.DEFAULT)

                repository.updateSettings {
                    it.copy(
                        doomscrollLimit = Duration.ofMinutes(90),
                        bedtime = LocalTime.of(22, 45),
                        wakeTime = LocalTime.of(6, 30),
                        cooldown = Duration.ofMinutes(15),
                    )
                }

                val updated = awaitItem()
                assertThat(updated.doomscrollLimit).isEqualTo(Duration.ofMinutes(90))
                assertThat(updated.bedtime).isEqualTo(LocalTime.of(22, 45))
                assertThat(updated.wakeTime).isEqualTo(LocalTime.of(6, 30))
                assertThat(updated.cooldown).isEqualTo(Duration.ofMinutes(15))
            }
        }

    @Test
    fun `reset restores defaults but keeps the feature switch`() =
        runTest {
            repository.setFloatingMochiEnabled(true)
            repository.updateSettings { it.copy(doomscrollLimit = Duration.ofMinutes(30)) }

            repository.resetToDefaults()

            assertThat(repository.settings.first()).isEqualTo(NudgeSettings.DEFAULT)
            assertThat(repository.floatingMochiEnabled.first()).isTrue()
        }

    @Test
    fun `last nudge time round-trips`() =
        runTest {
            val at = LocalDateTime.of(2026, 3, 10, 23, 15, 30)

            repository.recordNudge(at)

            assertThat(repository.lastNudgeAt.first()).isEqualTo(at)
        }
}
