package com.ballog.mobile.navigation

// 각 화면의 route 이름을 관리할 sealed class 또는 객체 정의 예정
object Routes {
    // 온보딩 & 인증
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val SIGNUP_EMAIL_VERIFICATION = "signup/email-verification"
    const val SIGNUP_NICKNAME = "signup/nickname"
    const val SIGNUP_BIRTHDAY = "signup/birthday"
    const val SIGNUP_PROFILE_IMAGE = "signup/profile-image"

    // 메인 탭
    const val MAIN = "main"
    const val HOME = "home"

    // 마이페이지
    const val MYPAGE = "mypage"
    const val PROFILE_EDIT = "profile/edit"
    const val MYPAGE_LIKED_VIDEOS = "mypage/liked-videos"

    // 매치 데이터
    const val MATCH_DATA = "match_data"

    // 그 외 다른 route 상수들도 여기에 추가 예정
    const val PERMISSION_CHECK = "permission_check"
    const val PERMISSION_REQUEST = "permission_request"
    const val SAMSUNG_HEALTH_GUIDE = "samsung_health_guide"
}
