package com.ballog.mobile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ballog.mobile.BallogApplication
import com.ballog.mobile.data.api.RetrofitInstance
import com.ballog.mobile.data.dto.EmailSendRequest
import com.ballog.mobile.data.dto.EmailVerifyRequest
import com.ballog.mobile.data.dto.LoginRequest
import com.ballog.mobile.data.dto.SignUpRequest
import com.ballog.mobile.data.model.*
import com.ballog.mobile.util.ImageUtils
import com.ballog.mobile.util.S3Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val tokenManager = BallogApplication.getInstance().tokenManager
    private val authApi = RetrofitInstance.authApi

    // 인증 상태 관리
    private val _authState = MutableStateFlow<AuthResult<Auth>>(AuthResult.Error(""))
    val authState: StateFlow<AuthResult<Auth>> = _authState.asStateFlow()

    // 이미지 업로드 상태 관리
    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Initial)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    // S3 초기화 여부
    private var isS3Initialized = false

    // 인증 상태 초기화
    fun resetAuthState() {
        _authState.value = AuthResult.Error("")
    }

    // 회원가입 상태 관리
    private val _signUpState = MutableStateFlow<AuthResult<Unit>>(AuthResult.Error(""))
    val signUpState: StateFlow<AuthResult<Unit>> = _signUpState.asStateFlow()

    // 회원가입 완료 후 로그인 화면에서 사용할 데이터
    private val _lastSignUpCredentials = MutableStateFlow<Pair<String, String>?>(null)
    val lastSignUpCredentials: StateFlow<Pair<String, String>?> = _lastSignUpCredentials.asStateFlow()

    // 이메일 인증 상태 관리
    private val _emailVerificationState = MutableStateFlow<EmailVerificationResult>(EmailVerificationResult.Success)
    val emailVerificationState: StateFlow<EmailVerificationResult> = _emailVerificationState.asStateFlow()

    // 회원가입 진행 상태
    private val _signUpProgress = MutableStateFlow(SignUpProgress.EMAIL_PASSWORD)
    val signUpProgress: StateFlow<SignUpProgress> = _signUpProgress.asStateFlow()

    // 회원가입 데이터
    private val _signUpData = MutableStateFlow(
        SignUpData(
            email = "",
            password = "",
            nickname = "",
            birthDate = "",
            profileImageUrl = null
        )
    )
    val signUpData: StateFlow<SignUpData> = _signUpData.asStateFlow()

    // 이메일 인증 상태
    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    // 프로필 이미지 원본 Uri
    private var _profileImageUri: Uri? = null

    // 로그아웃/회원탈퇴 상태 관리
    private val _signOutState = MutableStateFlow<AuthResult<Unit>>(AuthResult.Error(""))
    val signOutState: StateFlow<AuthResult<Unit>> = _signOutState.asStateFlow()

    /**
     * S3 초기화 (처음 한 번만 호출)
     */
    fun initS3(context: Context) {
        if (!isS3Initialized) {
            // 암호화 옵션을 활성화하여 S3Utils 초기화
            S3Utils.init(context)
            isS3Initialized = true
        }
    }

    /**
     * 프로필 이미지 업로드
     * 1. Uri를 File로 변환
     * 2. 이미지 리사이징
     * 3. AWS S3에 직접 업로드
     * 4. 업로드된 URL을 회원가입 데이터에 설정
     */
    suspend fun uploadProfileImage(context: Context, imageUri: Uri?) {
        try {
            if (imageUri == null) {
                _imageUploadState.value = ImageUploadState.Error("이미지가 선택되지 않았습니다")
                return
            }
            
            // S3 초기화 확인
            if (!isS3Initialized) {
                initS3(context)
            }
            
            _profileImageUri = imageUri
            _imageUploadState.value = ImageUploadState.Loading
            
            // Uri를 File로 변환
            val imageFile = ImageUtils.uriToFile(context, imageUri)
            if (imageFile == null) {
                _imageUploadState.value = ImageUploadState.Error("이미지 파일 변환에 실패했습니다")
                return
            }
            
            // 이미지 리사이징
            val resizedFile = ImageUtils.resizeImage(imageFile) ?: imageFile
            
            try {
                // S3에 직접 업로드
                val imageUrl = S3Utils.uploadImageToS3(resizedFile, "profile")
                
                // 업로드된 URL을 회원가입 데이터에 설정
                setSignUpProfileImageUrl(imageUrl)
                _imageUploadState.value = ImageUploadState.Success(imageUrl)
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState.Error("S3 업로드 중 오류 발생: ${e.message}")
            } finally {
                // 임시 파일 삭제
                resizedFile.delete()
                if (resizedFile != imageFile) {
                    imageFile.delete()
                }
            }
            
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요"
                is java.net.SocketTimeoutException -> "서버 응답이 지연되고 있습니다"
                else -> e.message ?: "이미지 업로드 중 오류가 발생했습니다"
            }
            _imageUploadState.value = ImageUploadState.Error(errorMessage)
        }
    }
    
    // 회원가입 데이터 유효성 검사
    private fun validateSignUpData(): String? {
        return when {
            _signUpData.value.email.isBlank() -> "이메일을 입력해주세요"
            _signUpData.value.password.isBlank() -> "비밀번호를 입력해주세요"
            _signUpData.value.nickname.isBlank() -> "닉네임을 입력해주세요"
            _signUpData.value.birthDate.isBlank() -> "생년월일을 입력해주세요"
            else -> null
        }
    }

    // 회원가입 데이터 설정 함수들
    fun setSignUpEmailAndPassword(email: String, password: String) {
        println("AuthViewModel - Setting signup email and password: email=$email")
        _signUpData.update { it.copy(email = email, password = password) }
        println("AuthViewModel - Progress updated to: ${_signUpProgress.value}")
    }

    // 이메일 인증 화면으로 진행
    fun proceedToEmailVerification() {
        println("AuthViewModel - Proceeding to email verification")
        _signUpProgress.value = SignUpProgress.EMAIL_VERIFICATION
        println("AuthViewModel - Progress updated to: ${_signUpProgress.value}")
    }

    fun completeVerification() {
        println("AuthViewModel - Before completeVerification: progress=${_signUpProgress.value}")
        _isEmailVerified.value = true
        _signUpProgress.value = SignUpProgress.NICKNAME
        println("AuthViewModel - After completeVerification: progress=${_signUpProgress.value}")
    }

    fun setSignUpNickname(nickname: String) {
        println("AuthViewModel - Before setSignUpNickname: progress=${_signUpProgress.value}")
        _signUpData.value = _signUpData.value.copy(nickname = nickname)
        _signUpProgress.value = SignUpProgress.BIRTH_DATE
        println("AuthViewModel - After setSignUpNickname: progress=${_signUpProgress.value}")
    }

    fun setSignUpBirthDate(birthDate: String) {
        println("AuthViewModel - Setting birth date: $birthDate")
        _signUpData.value = _signUpData.value.copy(birthDate = birthDate)
        _signUpProgress.value = SignUpProgress.PROFILE
    }

    fun setSignUpProfileImageUrl(url: String?) {
        println("AuthViewModel - Setting profile image URL: $url")
        _signUpData.value = _signUpData.value.copy(profileImageUrl = url)
    }

    // 회원가입 진행 상태 초기화
    fun resetSignUpProgress() {
        _signUpProgress.value = SignUpProgress.EMAIL_PASSWORD
        _signUpData.value = SignUpData(
            email = "",
            password = "",
            nickname = "",
            birthDate = "",
            profileImageUrl = null
        )
        _isEmailVerified.value = false
        _signUpState.value = AuthResult.Error("")
        _emailVerificationState.value = EmailVerificationResult.Success
    }

    // 회원가입 완료
    suspend fun signUp() {
        try {
            _signUpState.value = AuthResult.Loading

            // 데이터 유효성 검사
            validateSignUpData()?.let { error ->
                _signUpState.value = AuthResult.Error(error)
                return
            }

            val request = SignUpRequest(
                email = _signUpData.value.email,
                password = _signUpData.value.password,
                nickname = _signUpData.value.nickname,
                birthDate = _signUpData.value.birthDate,
                profileImageUrl = _signUpData.value.profileImageUrl
            )

            val response = authApi.signUp(request)
            
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                // 회원가입 성공 시 이메일과 비밀번호 저장
                _lastSignUpCredentials.value = Pair(_signUpData.value.email, _signUpData.value.password)
                println("AuthViewModel - Saved credentials after signup: ${_signUpData.value.email}")
                
                _signUpProgress.value = SignUpProgress.COMPLETED
                _signUpState.value = AuthResult.Success(Unit)
                resetSignUpData()
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "이미 가입된 이메일입니다"
                    400 -> response.body()?.message ?: "잘못된 요청입니다"
                    else -> response.body()?.message ?: "회원가입에 실패했습니다"
                }
                _signUpState.value = AuthResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요"
                is java.net.SocketTimeoutException -> "서버 응답이 지연되고 있습니다"
                else -> e.message ?: "회원가입 중 오류가 발생했습니다"
            }
            _signUpState.value = AuthResult.Error(errorMessage)
        }
    }

    // 이메일 인증 메일 발송
    suspend fun sendVerificationEmail(email: String) {
        println("AuthViewModel - sendVerificationEmail called with email: $email")
        try {
            // 상태 초기화
            _emailVerificationState.value = EmailVerificationResult.Loading
            
            println("AuthViewModel - Sending verification email to: $email")
            val request = EmailSendRequest(email = email)
            val response = authApi.sendEmail(request)
            
            println("AuthViewModel - Email verification response: ${response.isSuccessful}, body: ${response.body()}")
            
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                println("AuthViewModel - Email sent successfully")
                // 이메일 발송 성공 시에는 EmailVerificationResult.Loading 상태 유지
                // 이렇게 하면 실제 인증 코드 검증 시까지 Success로 바뀌지 않음
                _signUpProgress.value = SignUpProgress.EMAIL_VERIFICATION
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "이미 인증된 이메일입니다"
                    429 -> "잠시 후 다시 시도해주세요"
                    else -> response.body()?.message ?: "이메일 발송에 실패했습니다"
                }
                println("AuthViewModel - Email sending failed: $errorMessage")
                _emailVerificationState.value = EmailVerificationResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요"
                is java.net.SocketTimeoutException -> "서버 응답이 지연되고 있습니다"
                else -> e.message ?: "이메일 발송 중 오류가 발생했습니다"
            }
            println("AuthViewModel - Email sending exception: ${e.message}")
            e.printStackTrace()
            _emailVerificationState.value = EmailVerificationResult.Error(errorMessage)
        }
    }

    // 이메일 인증 코드 확인 (suspend 함수로 변경)
    suspend fun verifyEmail(email: String, code: String) {
        try {
            _emailVerificationState.value = EmailVerificationResult.Loading
            
            val request = EmailVerifyRequest(email = email, authCode = code)
            val response = authApi.verifyEmail(request)
            
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                _emailVerificationState.value = EmailVerificationResult.Success
                _isEmailVerified.value = true
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "유효하지 않은 인증 코드입니다"
                    404 -> "인증 메일이 발송되지 않았습니다"
                    410 -> "인증 코드가 만료되었습니다"
                    else -> response.body()?.message ?: "인증에 실패했습니다"
                }
                _emailVerificationState.value = EmailVerificationResult.Error(errorMessage)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // 취소 예외는 그대로 전파 (아무 작업도 하지 않음)
            // 하지만 Loading 상태로 남아있다면 Success로 변경
            if (_emailVerificationState.value is EmailVerificationResult.Loading) {
                _emailVerificationState.value = EmailVerificationResult.Success
            }
            throw e
        } catch (e: Exception) {
            // 코루틴 스코프 관련 오류는 무시
            if (e.message?.contains("coroutine scope", ignoreCase = true) == true) {
                _emailVerificationState.value = EmailVerificationResult.Success
                return
            }
            
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요"
                is java.net.SocketTimeoutException -> "서버 응답이 지연되고 있습니다"
                else -> e.message ?: "인증 중 오류가 발생했습니다"
            }
            _emailVerificationState.value = EmailVerificationResult.Error(errorMessage)
        }
    }

    // 비동기 이메일 인증 (비suspend 함수 - 코루틴 취소 무시)
    fun verifyEmailNonSuspend(email: String, code: String) {
        viewModelScope.launch {
            try {
                verifyEmail(email, code)
            } catch (e: kotlinx.coroutines.CancellationException) {
                // 취소는 정상적인 동작이므로 무시하고 상태를 변경하지 않음
                // 이미 취소된 경우 오류 상태 초기화
                if (_emailVerificationState.value is EmailVerificationResult.Loading) {
                    _emailVerificationState.value = EmailVerificationResult.Success
                }
            } catch (e: Exception) {
                // 코루틴 스코프 관련 예외는 무시
                if (e.message?.contains("coroutine scope", ignoreCase = true) != true) {
                    // 다른 예외일 경우만 오류 상태 설정
                    _emailVerificationState.value = EmailVerificationResult.Error(e.message ?: "인증 중 오류가 발생했습니다")
                } else {
                    // 코루틴 스코프 오류 시 Success 상태로 설정
                    _emailVerificationState.value = EmailVerificationResult.Success
                }
            }
        }
    }

    // 회원가입 데이터 초기화
    private fun resetSignUpData() {
        _signUpData.value = SignUpData(
            email = "",
            password = "",
            nickname = "",
            birthDate = "",
            profileImageUrl = null
        )
        _isEmailVerified.value = false
        _signUpProgress.value = SignUpProgress.EMAIL_PASSWORD
    }

    // 로그인 처리
    fun login(context: Context, email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthResult.Loading
                val loginData = LoginData(email = email, password = password)
                val request = LoginRequest(email = loginData.email, password = loginData.password)
                val response = authApi.login(request)
                val loginResult = when (response.body()?.code) {
                    200 -> {
                        response.body()?.result?.let { loginResponse ->
                            val auth = Auth(
                                accessToken = loginResponse.accessToken,
                                refreshToken = loginResponse.refreshToken
                            )
                            LoginResult.Success(auth)
                        } ?: LoginResult.Error("응답 데이터가 없습니다")
                    }
                    2000 -> LoginResult.RequireSignup
                    1003 -> LoginResult.WrongPassword
                    else -> LoginResult.Error(response.body()?.message ?: "로그인에 실패했습니다")
                }
                when (loginResult) {
                    is LoginResult.Success -> {
                        com.ballog.mobile.data.util.OnboardingPrefs.clearAll(context)
                        com.ballog.mobile.data.util.OnboardingPrefs.setOnboardingCompleted(context, true)
                        tokenManager.saveTokens(
                            accessToken = loginResult.auth.accessToken,
                            refreshToken = loginResult.auth.refreshToken
                        )
                        _authState.value = AuthResult.Success(loginResult.auth)
                    }
                    is LoginResult.RequireSignup -> {
                        _authState.value = AuthResult.Error("회원가입이 필요합니다")
                    }
                    is LoginResult.WrongPassword -> {
                        _authState.value = AuthResult.Error("비밀번호가 일치하지 않습니다")
                    }
                    is LoginResult.Error -> {
                        _authState.value = AuthResult.Error(loginResult.message)
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error(e.message ?: "로그인 중 오류가 발생했습니다")
            } finally {
                if (_authState.value is AuthResult.Loading) {
                    _authState.value = AuthResult.Error("로그인 처리 중 오류가 발생했습니다")
                }
            }
        }
    }

    // 로그아웃 처리 (회원탈퇴처럼 내부에서 launch)
    fun logout() {
        viewModelScope.launch {
            try {
                println("AuthViewModel - Starting logout process")
                _authState.value = AuthResult.Loading
                val token = tokenManager.getAccessToken().firstOrNull()
                println("AuthViewModel - Current token exists: ${token != null}")
                if (token == null) {
                    println("AuthViewModel - No token found, clearing tokens")
                    tokenManager.clearTokens()
                    _authState.value = AuthResult.Error("로그인이 필요합니다")
                    return@launch
                }
                println("AuthViewModel - Calling logout API")
                val response = authApi.logout("Bearer $token")
                // API 응답과 관계없이 토큰 삭제
                println("AuthViewModel - Clearing tokens regardless of API response")
                tokenManager.clearTokens()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    println("AuthViewModel - Logout API success")
                    _authState.value = AuthResult.Error("로그인이 필요합니다")
                } else {
                    println("AuthViewModel - Logout API failed: ${response.body()?.message}")
                    _authState.value = AuthResult.Error(response.body()?.message ?: "로그아웃에 실패했습니다")
                }
            } catch (e: Exception) {
                println("AuthViewModel - Logout error: ${e.message}")
                // 에러가 발생해도 토큰 삭제
                tokenManager.clearTokens()
                _authState.value = AuthResult.Error(e.message ?: "로그아웃 중 오류가 발생했습니다")
            }
        }
    }

    // 회원탈퇴(탈퇴 API 호출)
    fun signOut() {
        viewModelScope.launch {
            _signOutState.value = AuthResult.Loading
            try {
                val token = tokenManager.getAccessToken().firstOrNull()
                if (token == null) {
                    _signOutState.value = AuthResult.Error("로그인이 필요합니다")
                    return@launch
                }
                val response = authApi.signOut("Bearer $token")
                if (response.isSuccessful) {
                    // 토큰 등 인증 정보 삭제
                    tokenManager.clearTokens()
                    _signOutState.value = AuthResult.Success(Unit)
                } else {
                    _signOutState.value = AuthResult.Error("회원탈퇴 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                _signOutState.value = AuthResult.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }
}
