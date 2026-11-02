package com.mochi.feature.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mochi.core.data.settings.SettingsRepository
import com.mochi.core.model.UnlockQuestion
import com.mochi.core.model.UnlockQuiz
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val questions: List<UnlockQuestion> = UnlockQuiz.pick()
        private var index = 0

        private val state =
            MutableStateFlow(
                LockUiState(step = 1, total = questions.size, mochiSays = questions.first().prompt),
            )
        val uiState: StateFlow<LockUiState> = state.asStateFlow()

        fun onAnswerChanged(text: String) {
            state.update { it.copy(answer = text) }
        }

        fun onSubmit() {
            val current = state.value
            if (current.unlocked) return
            if (!UnlockQuiz.isAcceptable(current.answer)) {
                state.update { it.copy(mochiSays = UnlockQuiz.TOO_SHORT_REPLY) }
                return
            }

            val reaction = questions[index].reaction
            index++
            if (index < questions.size) {
                state.update {
                    it.copy(
                        step = index + 1,
                        answer = "",
                        mochiSays = "$reaction\n\n${questions[index].prompt}",
                    )
                }
            } else {
                viewModelScope.launch {
                    settingsRepository.setInstagramLocked(false)
                    state.update { it.copy(answer = "", mochiSays = "$reaction\n\n$UNLOCKED_REPLY", unlocked = true) }
                }
            }
        }

        private companion object {
            const val UNLOCKED_REPLY = "Fine. Instagram is yours again. Don't make me come back 😤"
        }
    }
