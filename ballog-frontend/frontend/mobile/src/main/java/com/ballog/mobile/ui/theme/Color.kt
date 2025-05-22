package com.ballog.mobile.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Gray scale colors
object Gray {
    val Gray100 = Color(0xFFFFFFFF)
    val Gray200 = Color(0xFFF2F5F8)
    val Gray300 = Color(0xFFECEEF0)
    val Gray400 = Color(0xFFD4D9DE)
    val Gray500 = Color(0xFF9BA0A5)
    val Gray600 = Color(0xFF2B2B2B)
    val Gray700 = Color(0xFF1B1C1D)
    val Gray800 = Color(0xFF000000)
}

// System colors
object System {
    val Red = Color(0xFFEA4335)
    val Yellow = Color(0xFFFCBC05)
    val Green = Color(0xFF34A853)
}

val Primary = Color(0xFF7EE4EA)
val OnPrimary = Color.White
val Background = Color(0xFFF2F2F2)
val Surface = Color.White

val LightColorScheme = lightColorScheme(
    primary = Primary,
    background = Gray.Gray100,
    surface = Gray.Gray100,
    onPrimary = Gray.Gray800,
    onSecondary = Gray.Gray800,
    onBackground = Gray.Gray800,
    onSurface = Gray.Gray800,
    error = System.Red,
    onError = Gray.Gray100
)

val DarkColorScheme = darkColorScheme(
    primary = Primary,
    background = Gray.Gray700,
    surface = Gray.Gray600,
    onPrimary = Gray.Gray100,
    onSecondary = Gray.Gray100,
    onBackground = Gray.Gray100,
    onSurface = Gray.Gray100,
    error = System.Red,
    onError = Gray.Gray100
)
