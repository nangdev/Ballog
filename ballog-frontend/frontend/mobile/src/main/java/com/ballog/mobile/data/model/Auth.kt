package com.ballog.mobile.data.model

// 인증 도메인 모델 정의 예정

data class Auth(
    val accessToken: String,
    val refreshToken: String
)

data class SignUpData(
    val email: String = "",
    val password: String = "",
    val nickname: String = "",
    val birthDate: String = "",
    val profileImageUrl: String? = null
)

data class LoginData(
    val email: String,
    val password: String
)

data class EmailVerification(
    val email: String,
    val authCode: String
)

// API 응답을 위한 래퍼 클래스
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

// 로그인 결과를 나타내는 sealed class
sealed class LoginResult {
    data class Success(val auth: Auth) : LoginResult()
    object RequireSignup : LoginResult()
    object WrongPassword : LoginResult()
    data class Error(val message: String) : LoginResult()
}

// 회원가입 진행 상태를 나타내는 enum
enum class SignUpProgress {
    EMAIL_PASSWORD,  // 이메일/비밀번호 입력
    EMAIL_VERIFICATION,
    NICKNAME,       // 닉네임 입력
    BIRTH_DATE,     // 생년월일 입력
    PROFILE,        // 프로필 이미지 설정
    COMPLETED       // 회원가입 완료
}

// 이메일 인증 결과를 나타내는 sealed class
sealed class EmailVerificationResult {
    object Success : EmailVerificationResult()
    data class Error(val message: String) : EmailVerificationResult()
    object Loading : EmailVerificationResult()
}
