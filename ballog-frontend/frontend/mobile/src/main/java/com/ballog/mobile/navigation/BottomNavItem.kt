package com.ballog.mobile.navigation

// 하단 네비게이션 바에 표시할 탭 아이템을 정의할 sealed class 예정
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val iconResId: Int
)
