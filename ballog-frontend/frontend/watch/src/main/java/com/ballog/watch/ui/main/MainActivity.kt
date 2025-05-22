package com.ballog.watch.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import com.ballog.watch.ui.theme.WatchTheme
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.ballog.watch.ui.components.HomeScreen
import notfound.ballog.presentation.screens.InstructionScreen
import com.ballog.watch.data.service.MeasurementScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    WatchTheme {
        val navController = rememberSwipeDismissableNavController()
        NavigationGraph(navController = navController)
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onMeasureClick = {
                    navController.navigate("instruction")
                }
            )
        }
        composable("instruction") {
            InstructionScreen(
                onContinueClick = {
                    navController.navigate("measurement")
                }
            )
        }
        composable("measurement") {
            MeasurementScreen(
                onComplete = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
