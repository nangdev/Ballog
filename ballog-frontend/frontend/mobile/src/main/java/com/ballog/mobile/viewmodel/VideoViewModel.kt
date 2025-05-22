package com.ballog.mobile.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.ballog.mobile.data.api.RetrofitInstance
import com.ballog.mobile.data.dto.*
import com.ballog.mobile.data.local.TokenManager
import com.ballog.mobile.ui.video.HighlightUiState
import com.ballog.mobile.ui.video.QuarterVideoData
import com.ballog.mobile.ui.video.VideoUiState
import com.ballog.mobile.util.AudioUtils
import com.ballog.mobile.util.S3Utils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.media3.exoplayer.ExoPlayer

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val _videoUiState = MutableStateFlow(VideoUiState())
    val videoUiState: StateFlow<VideoUiState> = _videoUiState.asStateFlow()

    private val videoApi = RetrofitInstance.videoApi
    private val tokenManager = RetrofitInstance.getTokenManager()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ExoPlayer 상태를 관리하기 위한 상태 추가
    private val _shouldReleasePlayer = MutableStateFlow(false)
    val shouldReleasePlayer: StateFlow<Boolean> = _shouldReleasePlayer.asStateFlow()

    private var _currentExoPlayer = MutableStateFlow<ExoPlayer?>(null)
    private val currentExoPlayer: StateFlow<ExoPlayer?> = _currentExoPlayer.asStateFlow()
    
    // 하이라이트 카드에서 시크했는지 여부 추적
    private val _isSeekingFromHighlight = MutableStateFlow(false)
    val isSeekingFromHighlight: StateFlow<Boolean> = _isSeekingFromHighlight.asStateFlow()

    fun setError(message: String?) {
        _error.value = message
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    // ExoPlayer 해제 상태 초기화
    fun resetPlayerRelease() {
        _shouldReleasePlayer.value = false
    }

    /**
     * 쿼터 영상 및 하이라이트 조회
     */
    fun getMatchVideos(matchId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = "Bearer ${tokenManager.getAccessTokenBlocking()}"
                val response = videoApi.getMatchVideos(token, matchId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    if (result != null) {
                        Log.d("VideoViewModel", "✅ 쿼터 영상 조회 성공 - 총 ${result.totalQuarters}쿼터")
                        
                        // quarterList가 null인 경우 빈 리스트로 처리
                        val quarterList = result.quarterList
                        val quarterListStr = if (quarterList.isEmpty()) "비어 있음" else quarterList.toString()
                        Log.d("VideoViewModel", "📋 quarterList: $quarterListStr")
                        
                        val mappedQuarterList = quarterList.map { it.toQuarterVideoData() }
                        
                        _videoUiState.value = VideoUiState(
                            totalQuarters = result.totalQuarters,
                            quarterList = mappedQuarterList
                        )
                    } else {
                        Log.e("VideoViewModel", "❌ API 응답 결과가 null입니다")
                        _error.value = "데이터를 불러올 수 없습니다"
                    }
                } else {
                    val msg = response.body()?.message ?: "쿼터별 영상 조회 실패"
                    Log.e("VideoViewModel", "❌ API 실패 - $msg")
                    _error.value = msg
                    Log.e("VideoViewModel", "⚠️ raw error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "🔥 예외 발생 (getMatchVideos)", e)
                _error.value = "API 호출 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Presigned URL을 통한 쿼터 영상 업로드
     */
    fun uploadQuarterVideo(
        context: Context,
        file: File,
        matchId: Int,
        quarterNumber: Int,
        duration: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val request = PresignedVideoUploadRequest(
                    fileName = file.name
                )

                val json = Gson().toJson(request)
                Log.d("VideoViewModel", "📤 Presigned URL 요청 바디: $json")

                // 1. Presigned URL 발급 요청
                val token = "Bearer ${tokenManager.getAccessTokenBlocking()}"
                val response = videoApi.requestUploadUrl(token, request)
                
                Log.d("VideoViewModel", "📥 Presigned URL 응답: isSuccess=${response.body()?.isSuccess}, code=${response.body()?.code}")
                Log.d("VideoViewModel", "📥 응답 메시지: ${response.body()?.message}")
                
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val presignedResponse = response.body()?.result
                    val presignedUrl = presignedResponse?.s3Url
                    
                    Log.d("VideoViewModel", "📥 S3 URL: $presignedUrl")
                    
                    if (presignedUrl != null && presignedUrl.isNotEmpty()) {
                        Log.d("VideoViewModel", "✅ Presigned URL 수신 성공: $presignedUrl")
                        Log.d("VideoViewModel", "📦 S3 업로드 시작")

                        // 2. 파일을 S3에 업로드
                        val uploadSuccess = withContext(Dispatchers.IO) {
                            S3Utils.putFileToPresignedUrl(presignedUrl, file)
                        }

                        if (uploadSuccess) {
                            Log.d("VideoViewModel", "✅ S3 업로드 성공")
                            
                            // presigned URL에서 쿼리 파라미터 제거
                            val parts = presignedUrl.split("?")
                            val baseS3Url = if (parts.isNotEmpty()) parts[0] else presignedUrl
                            Log.d("VideoViewModel", "🔗 저장할 영상 URL: $baseS3Url")
                            
                            // 3. 영상 저장 요청
                            val saveRequest = SaveVideoRequest(
                                matchId = matchId,
                                quarterNumber = quarterNumber,
                                duration = duration,
                                videoUrl = baseS3Url
                            )
                            
                            val saveResponse = videoApi.saveVideo(token, saveRequest)
                            if (saveResponse.isSuccessful && saveResponse.body()?.isSuccess == true) {
                                Log.d("VideoViewModel", "✅ 영상 저장 성공")
                                
                                // 매치 비디오 목록 조회하여 방금 저장한 영상의 ID 찾기
                                Log.d("VideoViewModel", "🔍 저장된 영상의 ID 검색 시작")
                                Log.d("VideoViewModel", "🔍 찾을 영상 URL: $baseS3Url")
                                
                                // 매치 비디오 목록 조회
                                val matchResponse = videoApi.getMatchVideos(token, matchId)
                                if (matchResponse.isSuccessful && matchResponse.body()?.isSuccess == true) {
                                    val listResult = matchResponse.body()?.result
                                    if (listResult != null) {
                                        val quarterList = listResult.quarterList
                                        
                                        Log.d("VideoViewModel", "📋 매치 영상 목록 조회 성공 - ${quarterList.size}개 쿼터")
                                        
                                        // 방금 저장한 영상 찾기
                                        val savedVideo = quarterList.find { video -> video.videoUrl == baseS3Url }
                                        val videoId = savedVideo?.videoId
                                        
                                        if (videoId != null) {
                                            Log.d("VideoViewModel", "✅ 저장된 영상 ID 찾음: $videoId")
                                            
                                            // 4. 오디오 파일 추출
                                            Log.d("VideoViewModel", "🎵 오디오 추출 프로세스 시작")
                                            Log.d("VideoViewModel", "📁 원본 비디오 파일: ${file.absolutePath}")
                                            Log.d("VideoViewModel", "📊 비디오 파일 크기: ${file.length() / 1024}KB")
                                            
                                            val audioFile = AudioUtils.extractAudioToM4a(context, file)
                                            if (audioFile != null) {
                                                Log.d("VideoViewModel", "✅ 오디오 파일 추출 성공")
                                                Log.d("VideoViewModel", "📁 추출된 오디오 파일: ${audioFile.absolutePath}")
                                                Log.d("VideoViewModel", "📊 오디오 파일 크기: ${audioFile.length() / 1024}KB")
                                                
                                                try {
                                                    // 5. 하이라이트 자동 추출 요청
                                                    Log.d("VideoViewModel", "🎯 하이라이트 자동 추출 시작")
                                                    Log.d("VideoViewModel", "📤 하이라이트 추출 요청: videoId=$videoId")
                                                    
                                                    // 파일 파트
                                                    val audioRequestBody = audioFile.asRequestBody("audio/m4a".toMediaType())
                                                    val filePart = MultipartBody.Part.createFormData("file", audioFile.name, audioRequestBody)
                                                    
                                                    // API 호출
                                                    val extractionResponse = videoApi.extractHighlights(
                                                        token = token,
                                                        file = filePart,
                                                        videoId = videoId
                                                    )
                                                    
                                                    if (extractionResponse.isSuccessful && extractionResponse.body()?.isSuccess == true) {
                                                        Log.d("VideoViewModel", "✅ 하이라이트 추출 성공")
                                                        val highlights = extractionResponse.body()?.result ?: emptyList()
                                                        
                                                        Log.d("VideoViewModel", "📋 추출된 하이라이트 수: ${highlights.size}")
                                                        highlights.forEachIndexed { index, highlight ->
                                                            Log.d("VideoViewModel", "🎯 하이라이트 #${(index + 1)}")
                                                            Log.d("VideoViewModel", "- 시작 시간: ${highlight.startTime}")
                                                            Log.d("VideoViewModel", "- 종료 시간: ${highlight.endTime}")
                                                            Log.d("VideoViewModel", "- 신뢰도: ${highlight.confidence}")
                                                        }
                                                    } else {
                                                        Log.e("VideoViewModel", "❌ 하이라이트 추출 실패")
                                                        Log.e("VideoViewModel", "⚠️ 응답 코드: ${extractionResponse.code()}")
                                                        Log.e("VideoViewModel", "⚠️ 에러 메시지: ${extractionResponse.body()?.message}")
                                                        Log.e("VideoViewModel", "⚠️ 에러 바디: ${extractionResponse.errorBody()?.string()}")
                                                        _error.value = extractionResponse.body()?.message ?: "하이라이트 추출에 실패했습니다."
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("VideoViewModel", "🔥 하이라이트 추출 중 예외 발생", e)
                                                    Log.e("VideoViewModel", "⚠️ 예외 종류: ${e.javaClass.simpleName}")
                                                    Log.e("VideoViewModel", "⚠️ 예외 메시지: ${e.message}")
                                                    _error.value = "하이라이트 추출 중 오류가 발생했습니다: ${e.message}"
                                                } finally {
                                                    // 오디오 파일 삭제
                                                    Log.d("VideoViewModel", "🗑️ 임시 오디오 파일 삭제 시작")
                                                    val deleted = audioFile.delete()
                                                    if (deleted) {
                                                        Log.d("VideoViewModel", "✅ 임시 오디오 파일 삭제 성공")
                                                    } else {
                                                        Log.e("VideoViewModel", "⚠️ 임시 오디오 파일 삭제 실패")
                                                    }
                                                }
                                            } else {
                                                Log.e("VideoViewModel", "❌ 오디오 파일 추출 실패")
                                            }
                                        } else {
                                            Log.e("VideoViewModel", "❌ 저장된 영상을 찾을 수 없음")
                                            Log.d("VideoViewModel", "📋 조회된 영상 URL 목록:")
                                            quarterList.forEach { video ->
                                                val videoUrl = video.videoUrl ?: ""
                                                Log.d("VideoViewModel", "- $videoUrl")
                                            }
                                        }
                                    } else {
                                        Log.e("VideoViewModel", "❌ API 응답 결과가 null입니다")
                                    }
                                } else {
                                    Log.e("VideoViewModel", "❌ 매치 영상 목록 조회 실패")
                                    Log.e("VideoViewModel", "⚠️ 에러 메시지: ${matchResponse.body()?.message}")
                                }
                                
                                // 6. 매치 비디오 목록 갱신 (UI 업데이트)
                                getMatchVideos(matchId)
                            } else {
                                val errorMessage = saveResponse.body()?.message ?: "영상 저장 실패"
                                Log.e("VideoViewModel", "❌ 영상 저장 실패 - $errorMessage")
                                _error.value = errorMessage
                            }
                        } else {
                            Log.e("VideoViewModel", "⛔ S3 업로드 실패")
                            _error.value = "S3 업로드에 실패했습니다"
                        }
                    } else {
                        Log.e("VideoViewModel", "❌ Presigned URL이 비어 있음")
                        _error.value = "Presigned URL이 유효하지 않습니다"
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Presigned URL 요청 실패"
                    Log.e("VideoViewModel", "❌ Presigned URL 요청 실패 - $errorMessage")
                    _error.value = errorMessage
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "🔥 업로드 예외 발생", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteVideo(videoId: Int, matchId: Int) {
        viewModelScope.launch {
            try {
                Log.d("VideoViewModel", "🗑️ 영상 삭제 시작")
                Log.d("VideoViewModel", "📋 삭제할 영상 ID: $videoId")
                Log.d("VideoViewModel", "📋 매치 ID: $matchId")
                
                val token = "Bearer ${tokenManager.getAccessTokenBlocking()}"
                val response = videoApi.deleteVideo(token, videoId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("VideoViewModel", "✅ 영상 삭제 성공")
                    // ExoPlayer 해제 신호 전송
                    _shouldReleasePlayer.value = true
                    Log.d("VideoViewModel", "🎵 ExoPlayer 해제 신호 전송")
                    
                    Log.d("VideoViewModel", "🔄 영상 목록 새로고침 시작")
                    getMatchVideos(matchId)
                    Log.d("VideoViewModel", "✅ 영상 목록 새로고침 완료")
                } else {
                    val errorMessage = response.body()?.message ?: "영상 삭제 실패"
                    Log.e("VideoViewModel", "❌ 영상 삭제 실패 - $errorMessage")
                    Log.e("VideoViewModel", "⚠️ 에러 응답: ${response.errorBody()?.string()}")
                    _error.value = errorMessage
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "🔥 영상 삭제 중 예외 발생", e)
                Log.e("VideoViewModel", "⚠️ 예외 메시지: ${e.message}")
                _error.value = "영상 삭제 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    fun addHighlight(request: HighlightAddRequest, matchId: Int) {
        viewModelScope.launch {
            try {
                Log.d("VideoViewModel", "➕ 하이라이트 추가 시작")
                Log.d("VideoViewModel", "📋 요청 정보: videoId=${request.videoId}, name=${request.highlightName}, start=${request.startTime}, end=${request.endTime}")
                
                val token = "Bearer ${tokenManager.getAccessTokenBlocking()}"
                val response = videoApi.addHighlight(token, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()?.result
                    Log.d("VideoViewModel", "✅ 하이라이트 추가 성공: highlightId=${result?.highlightId}")
                    
                    // 전체 매치 데이터 새로고침
                    Log.d("VideoViewModel", "🔄 매치 데이터 새로고침 시작")
                    getMatchVideos(matchId)
                    Log.d("VideoViewModel", "✅ 매치 데이터 새로고침 완료")
                } else {
                    val errorMessage = response.body()?.message ?: "하이라이트 추가 실패"
                    Log.e("VideoViewModel", "❌ 하이라이트 추가 실패 - $errorMessage")
                    Log.e("VideoViewModel", "⚠️ 에러 응답: ${response.errorBody()?.string()}")
                    _error.value = errorMessage
                    throw Exception(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "🔥 하이라이트 추가 중 예외 발생", e)
                Log.e("VideoViewModel", "⚠️ 예외 메시지: ${e.message}")
                _error.value = e.message
                throw e
            }
        }
    }

    fun updateHighlight(request: HighlightUpdateRequest, matchId: Int) {
        viewModelScope.launch {
            try {
                Log.d("VideoViewModel", "✏️ 하이라이트 수정 시작")
                Log.d("VideoViewModel", "📋 수정 정보: highlightId=${request.highlightId}, name=${request.highlightName}, start=${request.startTime}, end=${request.endTime}")
                
                val token = "Bearer ${tokenManager.getAccessTokenBlocking()}"
                val response = videoApi.updateHighlight(token, request)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("VideoViewModel", "✅ 하이라이트 수정 성공")
                    
                    // 전체 매치 데이터 새로고침
                    Log.d("VideoViewModel", "🔄 매치 데이터 새로고침 시작")
                    getMatchVideos(matchId)
                    Log.d("VideoViewModel", "✅ 매치 데이터 새로고침 완료")
                } else {
                    val errorMessage = response.body()?.message ?: "하이라이트 수정 실패"
                    Log.e("VideoViewModel", "❌ 하이라이트 수정 실패 - $errorMessage")
                    Log.e("VideoViewModel", "⚠️ 에러 응답: ${response.errorBody()?.string()}")
                    _error.value = errorMessage
                    throw Exception(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "🔥 하이라이트 수정 중 예외 발생", e)
                Log.e("VideoViewModel", "⚠️ 예외 메시지: ${e.message}")
                _error.value = e.message
                throw e
            }
        }
    }

    fun deleteHighlight(highlightId: Int, matchId: Int) {
        viewModelScope.launch {
            try {
                Log.d("VideoViewModel", "🗑️ 하이라이트 삭제 시작")
                Log.d("VideoViewModel", "📋 삭제할 하이라이트 ID: $highlightId")
                Log.d("VideoViewModel", "📋 매치 ID: $matchId")
                
                val token = "Bearer ${tokenManager.getAccessTokenBlocking()}"
                val response = videoApi.deleteHighlight(token, highlightId)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("VideoViewModel", "✅ 하이라이트 삭제 성공")
                    getMatchVideos(matchId)
                } else {
                    val errorMessage = response.body()?.message ?: "하이라이트 삭제 실패"
                    Log.e("VideoViewModel", "❌ 하이라이트 삭제 실패 - $errorMessage")
                    Log.e("VideoViewModel", "⚠️ 에러 응답: ${response.errorBody()?.string()}")
                    _error.value = errorMessage
                }
            } catch (e: Exception) {
                Log.e("VideoViewModel", "🔥 하이라이트 삭제 중 예외 발생", e)
                Log.e("VideoViewModel", "⚠️ 예외 메시지: ${e.message}")
                _error.value = "하이라이트 삭제 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    /**
     * 기존 영상에 대해 하이라이트 자동 추출을 요청합니다.
     */
    fun requestHighlightExtraction(
        context: Context,
        videoId: Int,
        videoFile: File
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                Log.d("VideoViewModel", "🎵 오디오 추출 프로세스 시작")
                Log.d("VideoViewModel", "📁 원본 비디오 파일: ${videoFile.absolutePath}")
                Log.d("VideoViewModel", "📊 비디오 파일 크기: ${videoFile.length() / 1024}KB")
                
                // 1. 오디오 파일 추출
                val audioFile = AudioUtils.extractAudioToM4a(context, videoFile)
                if (audioFile != null) {
                    Log.d("VideoViewModel", "✅ 오디오 파일 추출 성공")
                    Log.d("VideoViewModel", "📁 추출된 오디오 파일: ${audioFile.absolutePath}")
                    Log.d("VideoViewModel", "📊 오디오 파일 크기: ${audioFile.length() / 1024}KB")
                    
                    try {
                        // 2. 하이라이트 자동 추출 요청
                        Log.d("VideoViewModel", "🎯 하이라이트 자동 추출 시작")
                        Log.d("VideoViewModel", "📤 하이라이트 추출 요청: videoId=$videoId")
                        
                        // 파일 파트
                        val audioRequestBody = audioFile.asRequestBody("audio/m4a".toMediaType())
                        val filePart = MultipartBody.Part.createFormData("file", audioFile.name, audioRequestBody)
                        
                        // API 호출
                        val token = "Bearer ${tokenManager.getAccessTokenBlocking()}"
                        val extractionResponse = videoApi.extractHighlights(
                            token = token,
                            file = filePart,
                            videoId = videoId
                        )
                        
                        if (extractionResponse.isSuccessful && extractionResponse.body()?.isSuccess == true) {
                            Log.d("VideoViewModel", "✅ 하이라이트 추출 성공")
                            val highlights = extractionResponse.body()?.result ?: emptyList()
                            
                            Log.d("VideoViewModel", "📋 추출된 하이라이트 수: ${highlights.size}")
                            highlights.forEachIndexed { index, highlight ->
                                Log.d("VideoViewModel", "🎯 하이라이트 #${(index + 1)}")
                                Log.d("VideoViewModel", "- 시작 시간: ${highlight.startTime}")
                                Log.d("VideoViewModel", "- 종료 시간: ${highlight.endTime}")
                                Log.d("VideoViewModel", "- 신뢰도: ${highlight.confidence}")
                            }
                        } else {
                            Log.e("VideoViewModel", "❌ 하이라이트 추출 실패")
                            Log.e("VideoViewModel", "⚠️ 응답 코드: ${extractionResponse.code()}")
                            Log.e("VideoViewModel", "⚠️ 에러 메시지: ${extractionResponse.body()?.message}")
                            Log.e("VideoViewModel", "⚠️ 에러 바디: ${extractionResponse.errorBody()?.string()}")
                            _error.value = extractionResponse.body()?.message ?: "하이라이트 추출에 실패했습니다."
                        }
                    } catch (e: Exception) {
                        Log.e("VideoViewModel", "🔥 하이라이트 추출 중 예외 발생", e)
                        Log.e("VideoViewModel", "⚠️ 예외 종류: ${e.javaClass.simpleName}")
                        Log.e("VideoViewModel", "⚠️ 예외 메시지: ${e.message}")
                        _error.value = "하이라이트 추출 중 오류가 발생했습니다: ${e.message}"
                    } finally {
                        // 오디오 파일 삭제
                        Log.d("VideoViewModel", "🗑️ 임시 오디오 파일 삭제 시작")
                        val deleted = audioFile.delete()
                        if (deleted) {
                            Log.d("VideoViewModel", "✅ 임시 오디오 파일 삭제 성공")
                        } else {
                            Log.e("VideoViewModel", "⚠️ 임시 오디오 파일 삭제 실패")
                        }
                    }
                } else {
                    Log.e("VideoViewModel", "❌ 오디오 파일 추출 실패")
                    _error.value = "오디오 파일 추출에 실패했습니다."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun VideoResponseDto.toQuarterVideoData(): QuarterVideoData {
        return QuarterVideoData(
            videoId = this.videoId ?: -1,
            quarterNumber = this.quarterNumber ?: 1,
            videoUrl = this.videoUrl?: "",
            highlights = this.highlightList.map { dto ->
                // HH:mm:ss -> mm:ss 변환
                val startParts = dto.startTime.split(":")
                val startTime = if (startParts.size >= 3) {
                    // HH:mm:ss 형식인 경우 mm:ss로 변환
                    val minutes = (startParts[0].toIntOrNull()?.times(60) ?: 0) + (startParts[1].toIntOrNull() ?: 0)
                    val seconds = startParts[2].toIntOrNull() ?: 0
                    val result = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
                    result
                } else if (startParts.size == 2) {
                    // mm:ss 형식인 경우 그대로 사용
                    val result = "${startParts[0].padStart(2, '0')}:${startParts[1].padStart(2, '0')}"
                    result
                } else {
                    "00:00"
                }
                
                val endParts = dto.endTime.split(":")
                val endTime = if (endParts.size >= 3) {
                    // HH:mm:ss 형식인 경우 mm:ss로 변환
                    val minutes = (endParts[0].toIntOrNull()?.times(60) ?: 0) + (endParts[1].toIntOrNull() ?: 0)
                    val seconds = endParts[2].toIntOrNull() ?: 0
                    val result = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
                    result
                } else if (endParts.size == 2) {
                    // mm:ss 형식인 경우 그대로 사용
                    val result = "${endParts[0].padStart(2, '0')}:${endParts[1].padStart(2, '0')}"
                    result
                } else {
                    "00:00"
                }
                
                val highlight = HighlightUiState(
                    id = dto.highlightId.toString(),
                    title = dto.highlightName,
                    startMin = startTime.trim().padStart(5, '0').substringBefore(":"),
                    startSec = startTime.trim().padStart(5, '0').substringAfter(":"),
                    endMin = endTime.trim().padStart(5, '0').substringBefore(":"),
                    endSec = endTime.trim().padStart(5, '0').substringAfter(":")
                )
                highlight
            }.sortedWith(compareBy(
                { it.startMin.toIntOrNull() ?: 0 },
                { it.startSec.toIntOrNull() ?: 0 },
                { it.endMin.toIntOrNull() ?: 0 },
                { it.endSec.toIntOrNull() ?: 0 },
                { it.title }
            )),
            showPlayer = false
        )
    }

    // ExoPlayer 인스턴스를 저장하는 메서드
    fun setCurrentExoPlayer(player: ExoPlayer) {
        _currentExoPlayer.value = player
    }

    // "mm:ss" 형식의 특정 타임스탬프로 이동하는 메서드
    fun seekToTimestamp(timestamp: String) {
        val currentPlayer = _currentExoPlayer.value ?: return
        
        try {
            // 타임스탬프 파싱
            val parts = timestamp.split(":")
            if (parts.size != 2) {
                Log.e("VideoViewModel", "❌ 타임스탬프 형식이 잘못되었습니다: $timestamp")
                return
            }
            
            val minutes = parts[0].trim().toIntOrNull() ?: 0
            val seconds = parts[1].trim().toIntOrNull() ?: 0
            
            // 밀리초로 변환
            val positionMs = (minutes * 60 + seconds) * 1000L
            
            // 하이라이트에서 호출된 시크임을 표시
            _isSeekingFromHighlight.value = true
            
            // 해당 위치로 이동
            Log.d("VideoViewModel", "🎯 타임스탬프로 이동: $timestamp (${positionMs}ms)")
            currentPlayer.seekTo(positionMs)
            
            // 플레이어가 보이고 재생 중인지 확인
            if (!currentPlayer.isPlaying) {
                currentPlayer.play()
            }
            
            // 짧은 지연 후 상태 초기화 (로딩이 충분히 오래 표시되도록)
            viewModelScope.launch {
                kotlinx.coroutines.delay(300)
                _isSeekingFromHighlight.value = false
            }
        } catch (e: Exception) {
            Log.e("VideoViewModel", "❌ 타임스탬프 이동 실패", e)
            _isSeekingFromHighlight.value = false
        }
    }
}
