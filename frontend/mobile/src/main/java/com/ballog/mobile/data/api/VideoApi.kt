package com.ballog.mobile.data.api

import com.ballog.mobile.data.dto.*
import com.ballog.mobile.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface VideoApi {

    // 1. 매치 영상 조회
    @GET("v1/videos/{matchId}")
    suspend fun getMatchVideos(
        @Header("Authorization") token: String,
        @Path("matchId") matchId: Int
    ): Response<ApiResponse<VideoListResult>>

    // 2. Presigned URL 발급
    @POST("v1/videos/url")
    suspend fun requestUploadUrl(
        @Header("Authorization") token: String,
        @Body request: PresignedVideoUploadRequest
    ): Response<ApiResponse<PresignedVideoUploadResponse>>

    // 3. 쿼터 영상 저장
    @POST("v1/videos")
    suspend fun saveVideo(
        @Header("Authorization") token: String,
        @Body request: SaveVideoRequest
    ): Response<ApiResponse<SaveVideoResult>>

    // 4. 쿼터 영상 삭제
    @DELETE("v1/videos/{videoId}")
    suspend fun deleteVideo(
        @Header("Authorization") token: String,
        @Path("videoId") videoId: Int
    ): Response<ApiResponse<Unit>>

    // 5. 하이라이트 수동 추가
    @POST("v1/videos/highlight")
    suspend fun addHighlight(
        @Header("Authorization") token: String,
        @Body request: HighlightAddRequest
    ): Response<ApiResponse<HighlightAddResult>>

    // 6. 하이라이트 수정
    @PATCH("v1/videos/highlight")
    suspend fun updateHighlight(
        @Header("Authorization") token: String,
        @Body request: HighlightUpdateRequest
    ): Response<ApiResponse<Unit>>

    // 7. 하이라이트 삭제
    @DELETE("v1/videos/highlight/{highlightId}")
    suspend fun deleteHighlight(
        @Header("Authorization") token: String,
        @Path("highlightId") highlightId: Int
    ): Response<ApiResponse<Unit>>

    // 8. 하이라이트 자동 추출
    @Multipart
    @POST("v1/videos/highlight/auto")
    suspend fun extractHighlights(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("videoId") videoId: Int
    ): Response<ApiResponse<List<ExtractedHighlight>>>
}
