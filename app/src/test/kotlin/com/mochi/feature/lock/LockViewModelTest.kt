package com.mochi.feature.lock

import com.google.common.truth.Truth.assertThat
import com.mochi.core.model.UnlockQuiz
import com.mochi.core.testing.FakeSettingsRepository
import com.mochi.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LockViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()
    private val viewModel = LockViewModel(repository)

    private fun answerCurrentQuestion() {
        viewModel.onAnswerChanged("this is a proper answer")
        viewModel.onSubmit()
    }

    @Test
    fun `starts on the first question`() {
        val state = viewModel.uiState.value

        assertThat(state.step).isEqualTo(1)
        assertThat(state.total).isEqualTo(UnlockQuiz.QUESTIONS_PER_UNLOCK)
        assertThat(state.mochiSays).isNotEmpty()
        assertThat(state.unlocked).isFalse()
    }

    @Test
    fun `a too short answer is mocked and does not advance`() {
        val firstQuestion = viewModel.uiState.value.mochiSays

        viewModel.onAnswerChanged("idk")
        viewModel.onSubmit()

        val state = viewModel.uiState.value
        assertThat(state.step).isEqualTo(1)
        assertThat(state.mochiSays).isEqualTo(UnlockQuiz.TOO_SHORT_REPLY)
        assertThat(state.mochiSays).isNotEqualTo(firstQuestion)
    }

    @Test
    fun `a good answer moves to the next question and clears the field`() {
        answerCurrentQuestion()

        val state = viewModel.uiState.value
        assertThat(state.step).isEqualTo(2)
        assertThat(state.answer).isEmpty()
        assertThat(state.unlocked).isFalse()
    }

    @Test
    fun `answering every question clears the lock`() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.setInstagramLocked(true)

            repeat(UnlockQuiz.QUESTIONS_PER_UNLOCK) { answerCurrentQuestion() }

            assertThat(viewModel.uiState.value.unlocked).isTrue()
            assertThat(repository.isLocked).isFalse()
        }

    @Test
    fun `the lock stays until the last question is answered`() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.setInstagramLocked(true)

            repeat(UnlockQuiz.QUESTIONS_PER_UNLOCK - 1) { answerCurrentQuestion() }

            assertThat(repository.isLocked).isTrue()
        }
}
