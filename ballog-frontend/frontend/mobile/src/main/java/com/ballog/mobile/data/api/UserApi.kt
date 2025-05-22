package com.ballog.mobile.data.api

import com.ballog.mobile.data.dto.AiRecommendDto
import com.ballog.mobile.data.dto.PlayerCardResponseDto
import com.ballog.mobile.data.dto.UserInfoResponse
import com.ballog.mobile.data.dto.UserStatisticsDto
import com.ballog.mobile.data.dto.UserUpdateRequest
import com.ballog.mobile.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApi {

    @GET("v1/users")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserInfoResponse>>

    @PATCH("v1/users")
    suspend fun updateUserInfo(
        @Header ("Authorization") token: String,
        @Body request: UserUpdateRequest
    ): Response<ApiResponse<Unit>>

    // 홈화면 사용자 통계 정보 가져오기
    @GET("v1/users/statistics")
    suspend fun getUserStatistics(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserStatisticsDto>>

    // 선수 카드 조회
    @GET("v1/users/player-card")
    suspend fun getPlayerCard(
        @Header("Authorization") token: String
    ): Response<ApiResponse<PlayerCardResponseDto>>

    @POST("v1/users/ai-recommend")
    suspend fun getAiRecommend(
        @Header("Authorization") token: String
    ): Response<ApiResponse<AiRecommendDto>>
}
