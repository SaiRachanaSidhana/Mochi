package com.mochi.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mochi.app.BuildConfig
import com.mochi.app.MainViewModel
import com.mochi.feature.home.HomeRoute
import com.mochi.feature.onboarding.OnboardingRoute
import com.mochi.feature.settings.SettingsRoute

@Composable
fun MochiApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val permissionsGranted by viewModel.requiredPermissionsGranted.collectAsStateWithLifecycle()
    val startDestination: Any = remember { if (permissionsGranted) HomeDestination else OnboardingDestination }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissions()
        onPauseOrDispose {}
    }

    LaunchedEffect(permissionsGranted) {
        val onOnboarding = navController.currentDestination?.hasRoute<OnboardingDestination>() == true
        if (!permissionsGranted && !onOnboarding) {
            navController.navigate(OnboardingDestination) { popUpTo(navController.graph.id) { inclusive = true } }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<OnboardingDestination> {
            OnboardingRoute(
                onFinished = {
                    navController.navigate(HomeDestination) {
                        popUpTo<OnboardingDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<HomeDestination> {
            HomeRoute(onOpenSettings = { navController.navigate(SettingsDestination) })
        }
        composable<SettingsDestination> {
            SettingsRoute(
                onBack = { navController.popBackStack() },
                showDebugTools = BuildConfig.DEBUG,
            )
        }
    }
}
