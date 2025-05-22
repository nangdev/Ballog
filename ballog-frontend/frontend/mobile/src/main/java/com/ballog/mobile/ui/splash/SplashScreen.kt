package com.ballog.mobile.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.ballog.mobile.navigation.Routes
import com.ballog.mobile.data.util.OnboardingPrefs
import com.ballog.mobile.BallogApplication
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
    var onboardingDone by remember { mutableStateOf<Boolean?>(null) }
    var permissionDone by remember { mutableStateOf<Boolean?>(null) }
    var guideDone by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val tokenManager = (context.applicationContext as BallogApplication).tokenManager
        val hasTokens = tokenManager.hasTokens().first()
        onboardingDone = OnboardingPrefs.isOnboardingCompleted(context)
        permissionDone = OnboardingPrefs.isPermissionCompleted(context)
        guideDone = OnboardingPrefs.isGuideCompleted(context)
        isLoggedIn = hasTokens
    }

    LaunchedEffect(isLoggedIn, onboardingDone, permissionDone, guideDone) {
        if (isLoggedIn != null && onboardingDone != null && permissionDone != null && guideDone != null) {
            if (isLoggedIn == true) {
                when {
                    onboardingDone != true -> navController.navigate(Routes.ONBOARDING) { popUpTo(0) { inclusive = true } }
                    permissionDone != true -> navController.navigate(Routes.PERMISSION_REQUEST) { popUpTo(0) { inclusive = true } }
                    guideDone != true -> navController.navigate(Routes.SAMSUNG_HEALTH_GUIDE) { popUpTo(0) { inclusive = true } }
                    else -> navController.navigate(Routes.MAIN) { popUpTo(0) { inclusive = true } }
                }
            } else {
                navController.navigate(Routes.ONBOARDING) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
} 
