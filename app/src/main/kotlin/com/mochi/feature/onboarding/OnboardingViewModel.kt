package com.mochi.feature.onboarding

import androidx.lifecycle.ViewModel
import com.mochi.core.data.permissions.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val permissionChecker: PermissionChecker,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState(permissionChecker.check()))
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        fun refresh() {
            _uiState.update { it.copy(permissions = permissionChecker.check()) }
        }

        fun onNotificationsResolved() {
            _uiState.update { it.copy(permissions = permissionChecker.check(), notificationsResolved = true) }
        }
    }
