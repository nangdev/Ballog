package com.ballog.mobile.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ballog.mobile.BallogApplication
import com.ballog.mobile.data.api.RetrofitInstance
import com.ballog.mobile.data.dto.PlayerCardResponseDto
import com.ballog.mobile.data.dto.UserStatisticsDto
import com.ballog.mobile.data.dto.UserUpdateRequest
import com.ballog.mobile.util.ImageUtils
import com.ballog.mobile.util.S3Utils
import com.ballog.mobile.data.model.User
import com.ballog.mobile.data.dto.toUser
import com.ballog.mobile.data.model.PlayerCardInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.ballog.mobile.data.dto.AiRecommendDto

class UserViewModel : ViewModel() {
    private val tokenManager = BallogApplication.getInstance().tokenManager
    private val userApi = RetrofitInstance.userApi

    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo: StateFlow<User?> = _userInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 이미지 업로드 상태
    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Initial)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    fun uploadProfileImage(context: Context, imageUri: Uri, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _imageUploadState.value = ImageUploadState.Loading
            try {
                val imageFile = ImageUtils.uriToFile(context, imageUri)
                if (imageFile == null) {
                    _imageUploadState.value = ImageUploadState.Error("이미지 파일 변환에 실패했습니다")
                    return@launch
                }
                val resizedFile = ImageUtils.resizeImage(imageFile) ?: imageFile
                val imageUrl = S3Utils.uploadImageToS3(resizedFile, "profile")
                _imageUploadState.value = ImageUploadState.Success(imageUrl)
                onSuccess(imageUrl)
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState.Error(e.message ?: "이미지 업로드 실패")
            }
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = tokenManager.getAccessToken().first()
                if (token == null) {
                    _error.value = "로그인이 필요합니다"
                    return@launch
                }
                val response = userApi.getUserInfo("Bearer $token")
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.isSuccess == true && apiResponse.result != null) {
                        _userInfo.value = apiResponse.result.toUser()
                    } else {
                        _error.value = apiResponse?.message ?: "유저 정보 조회 실패"
                    }
                } else {
                    _error.value = "서버 오류"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "알 수 없는 오류"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserInfo(nickname: String, birthDate: String, profileImageUrl: String) {
        viewModelScope.launch {
            println("[updateUserInfo] 호출됨: nickname=$nickname, birthDate=$birthDate, profileImageUrl=$profileImageUrl")
            _isLoading.value = true
            _error.value = null
            try {
                val token = tokenManager.getAccessToken().first()
                println("[updateUserInfo] 토큰: $token")
                if (token == null) {
                    _error.value = "로그인이 필요합니다"
                    println("[updateUserInfo] 토큰 없음, 종료")
                    return@launch
                }
                val request = UserUpdateRequest(nickname, birthDate, profileImageUrl)
                println("[updateUserInfo] PATCH 요청: $request")
                val response = userApi.updateUserInfo("Bearer $token", request)
                println("[updateUserInfo] 서버 응답: isSuccessful=${response.isSuccessful}, code=${response.code()}, message=${response.message()}")
                if (response.isSuccessful) {
                    println("[updateUserInfo] 성공, getUserInfo 호출")
                    getUserInfo()
                } else {
                    _error.value = "정보 수정 실패"
                    println("[updateUserInfo] 실패: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "알 수 없는 오류"
                println("[updateUserInfo] 예외 발생: ${e.message}")
            } finally {
                _isLoading.value = false
                println("[updateUserInfo] 종료, isLoading=false")
            }
        }
    }

    // 홈화면 사용자 통계 불러오기
    private val _userStatistics = MutableStateFlow<UserStatisticsDto?>(null)
    val userStatistics: StateFlow<UserStatisticsDto?> = _userStatistics.asStateFlow()

    fun fetchUserStatistics() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull() ?: return@launch
            try {
                val response = userApi.getUserStatistics("Bearer $token")
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    Log.d("ProfileViewModel", "✅ 사용자 통계 응답 성공: $result")
                    _userStatistics.value = result
                } else {
                    Log.e(
                        "ProfileViewModel",
                        "❌ 통계 불러오기 실패: response=${response}, code=${response.code()}, message=${response.body()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ 네트워크 오류: ${e.localizedMessage}", e)
            }
        }
    }

    // 내 카드 조회
    fun PlayerCardResponseDto.toDomain(): PlayerCardInfo {
        return PlayerCardInfo(
            nickname = nickname,
            profileImageUrl = profileImageUrl ?: "",
            stats = listOf(
                "Speed" to cardStats.speed.toString(),
                "Stamina" to cardStats.stamina.toString(),
                "Attack" to cardStats.attack.toString(),
                "Defense" to cardStats.defense.toString(),
                "Recovery" to cardStats.recovery.toString()
            )
        )
    }

    private val _playerCardInfo = MutableStateFlow<PlayerCardInfo?>(null)
    val playerCardInfo: StateFlow<PlayerCardInfo?> = _playerCardInfo

    private val _playerCardFetched = MutableStateFlow(false)
    val playerCardFetched: StateFlow<Boolean> = _playerCardFetched

    var lastPlayerCardFetchTime: Long = 0L

    fun shouldForceRefresh(): Boolean {
        val now = System.currentTimeMillis()
        return now - lastPlayerCardFetchTime > 60 * 60 * 1000 // 1시간 경과 시 강제 새로고침
    }

    fun fetchPlayerCardInfoIfNeeded() {
        if (_playerCardFetched.value && !shouldForceRefresh()) return
        fetchPlayerCardInfo()
    }

    fun refreshPlayerCardInfo() {
        fetchPlayerCardInfo(force = true)
    }

    private fun fetchPlayerCardInfo(force: Boolean = false) {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                Log.e("PlayerCard", "로그인 필요")
                return@launch
            }

            try {
                val response = userApi.getPlayerCard("Bearer $token")
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true && body.result != null) {
                        val domain = body.result.toDomain()
                        _playerCardInfo.value = domain
                        _playerCardFetched.value = true
                        lastPlayerCardFetchTime = System.currentTimeMillis() // ✅ 마지막 호출 시간 기록
                        Log.d("PlayerCard", "✅ 카드 불러오기 성공: $domain")
                    } else {
                        Log.e("PlayerCard", "❌ 실패: 코드=${body?.code}, 메시지=${body?.message}, 본문=${body}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PlayerCard", "❌ 응답 실패: code=${response.code()}, errorBody=$errorBody")
                }
            } catch (e: Exception) {
                Log.e("PlayerCard", "❌ 네트워크 오류: ${e.localizedMessage}", e)
            }
        }
    }

    // AI 추천 결과 상태
    private val _aiRecommend = MutableStateFlow<AiRecommendDto?>(null)
    val aiRecommend: StateFlow<AiRecommendDto?> = _aiRecommend.asStateFlow()

    fun fetchAiRecommend() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken().firstOrNull()
            if (token == null) {
                _error.value = "로그인이 필요합니다"
                return@launch
            }
            try {
                val response = userApi.getAiRecommend("Bearer $token")
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    println("API 요청 성공!")
                    _aiRecommend.value = response.body()?.result
                } else {
                    _error.value = response.body()?.message ?: "AI 추천 결과를 불러오지 못했습니다"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "AI 추천 결과 네트워크 오류"
            }
        }
    }
}

// 이미지 업로드 상태 sealed class (AuthViewModel 참고)
sealed class ImageUploadState {
    object Initial : ImageUploadState()
    object Loading : ImageUploadState()
    data class Success(val imageUrl: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}
