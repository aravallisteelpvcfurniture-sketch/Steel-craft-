package com.example.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AppViewModel
import com.example.ui.auth.AuthViewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.SignUpScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isGuest by authViewModel.isGuestMode.collectAsState()

    val startDestination = if (currentUser != null || isGuest) {
        NavRoutes.DASHBOARD
    } else {
        NavRoutes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { 300 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -300 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 300 }) + fadeOut() }
    ) {
        // 1. Login Screen
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(NavRoutes.SIGN_UP)
                },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onContinueAsGuest = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 2. Sign Up Screen
        composable(NavRoutes.SIGN_UP) {
            SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onContinueAsGuest = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 3. Main Dashboard Screen
        composable(NavRoutes.DASHBOARD) {
            MainDashboardScreen(
                appViewModel = appViewModel,
                authViewModel = authViewModel,
                onNavigateToAuth = {
                    authViewModel.exitGuestMode()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
    }
}
