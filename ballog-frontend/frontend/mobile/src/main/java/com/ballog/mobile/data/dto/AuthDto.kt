package com.ballog.mobile.data.dto

// 인증 관련 요청/응답 DTO 정의 예정

// 로그인 요청/응답
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class User(
    val uuid: String
)

// 회원가입 요청/응답
data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val birthDate: String,
    val profileImageUrl: String?
)

data class EmailSendRequest(
    val email: String
)

data class EmailVerifyRequest(
    val email: String,
    val authCode: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
