package com.ballog.mobile.data.dto

import android.net.Uri
import com.ballog.mobile.ui.video.HighlightUiState

data class VideoListResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: VideoListResult
)

data class VideoListResult(
    val totalQuarters: Int,
    val quarterList: List<VideoResponseDto>
)

data class VideoResponseDto(
    val videoId: Int?,
    val quarterNumber: Int?,
    val videoUrl: String?,
    val uploadSuccess: Boolean,
    val highlightList: List<HighlightDto>
)

data class HighlightDto(
    val highlightId: Int,
    val highlightName: String,
    val startTime: String,  // 예: "00:01:23"
    val endTime: String     // 예: "00:01:36"
)

data class PresignedVideoUploadRequest(
    val fileName: String
)

data class PresignedVideoUploadResponseWrapper(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: PresignedVideoUploadResponse
)

data class PresignedVideoUploadResponse(
    val s3Url: String,
    val videoId: Int
)

// 업로드 완료 후 영상 저장 요청 (기존 UploadSuccessRequest 대체)
data class SaveVideoRequest(
    val matchId: Int,
    val quarterNumber: Int,
    val duration: String,
    val videoUrl: String
)

data class SaveVideoResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: Any? // Map이나 다른 형식으로도 올 수 있으므로 Any?로 변경
)

// 서버 응답의 result가 이 형식이 아닐 수 있음
data class SaveVideoResult(
    val videoId: Int
)

data class HighlightAddRequest(
    val videoId: Int,
    val highlightName: String,
    val startTime: String,
    val endTime: String
)

data class HighlightUpdateRequest(
    val highlightId: Int,
    val highlightName: String,
    val startTime: String,
    val endTime: String
)

data class DeleteVideoRequest(
    val videoId: Int
)

data class DeleteHighlightRequest(
    val highlightId: Int
)

data class HighlightAddResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: HighlightAddResult
)

data class HighlightAddResult(
    val highlightId: Int
)

data class BaseResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: Any? = null
)

data class HighlightExtractionRequest(
    val videoId: Int,
    val videoUrl: String
)

data class HighlightExtractionResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: List<ExtractedHighlight>
)

data class ExtractedHighlight(
    val startTime: String,  // "00:01:23" 형식
    val endTime: String,    // "00:01:36" 형식
    val confidence: Float   // 하이라이트 신뢰도 (0.0 ~ 1.0)
)
