package com.mochi.app

import androidx.lifecycle.ViewModel
import com.mochi.core.data.permissions.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val permissionChecker: PermissionChecker,
    ) : ViewModel() {
        private val _requiredPermissionsGranted = MutableStateFlow(permissionChecker.check().requiredGranted)

        val requiredPermissionsGranted: StateFlow<Boolean> = _requiredPermissionsGranted.asStateFlow()

        fun refreshPermissions() {
            _requiredPermissionsGranted.value = permissionChecker.check().requiredGranted
        }
    }
