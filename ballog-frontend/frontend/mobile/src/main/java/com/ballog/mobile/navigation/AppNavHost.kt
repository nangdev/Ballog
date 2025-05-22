package com.ballog.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ballog.mobile.ui.auth.LoginScreen
import com.ballog.mobile.ui.auth.OnboardingScreen
import com.ballog.mobile.ui.auth.SignupBirthScreen
import com.ballog.mobile.ui.auth.SignupNicknameScreen
import com.ballog.mobile.ui.auth.SignupProfileScreen
import com.ballog.mobile.ui.auth.SignupScreen
import com.ballog.mobile.ui.auth.SignupVerificationScreen
import com.ballog.mobile.ui.main.MainScreen
import com.ballog.mobile.viewmodel.AuthViewModel
import com.ballog.mobile.ui.auth.PermissionRequestScreen
import com.ballog.mobile.ui.auth.SamsungHealthGuideScreen
import com.ballog.mobile.ui.home.HomeScreen
import com.ballog.mobile.ui.splash.SplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = "splash"
) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(navController)
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onEmailLoginClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(Routes.SIGNUP_EMAIL_VERIFICATION) {
            SignupVerificationScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(Routes.SIGNUP_NICKNAME) {
            SignupNicknameScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(Routes.SIGNUP_BIRTHDAY) {
            SignupBirthScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(Routes.SIGNUP_PROFILE_IMAGE) {
            SignupProfileScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(
            route = "${Routes.MAIN}?teamId={teamId}",
            arguments = listOf(
                navArgument("teamId") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId")
            MainScreen(
                navController = navController,
                viewModel = authViewModel,
                initialTeamId = teamId?.toIntOrNull()
            )
        }
        composable(Routes.PERMISSION_REQUEST) {
            PermissionRequestScreen(
                onConnectClick = {
                    navController.navigate(Routes.SAMSUNG_HEALTH_GUIDE) {
                        popUpTo(Routes.PERMISSION_REQUEST) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SAMSUNG_HEALTH_GUIDE) {
            SamsungHealthGuideScreen(
                onConnectClick = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SAMSUNG_HEALTH_GUIDE) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}
