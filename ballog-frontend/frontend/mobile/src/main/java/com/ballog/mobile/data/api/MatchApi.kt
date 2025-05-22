package com.ballog.mobile.data.api

import com.ballog.mobile.data.dto.MatchDetailResponseDto
import com.ballog.mobile.data.dto.MatchListResponse
import com.ballog.mobile.data.dto.MatchRegisterRequest
import com.ballog.mobile.data.dto.MatchReportResponse
import com.ballog.mobile.data.dto.TeamMatchRegisterRequest
import com.ballog.mobile.data.model.ApiResponse
import com.ballog.mobile.data.dto.DayMatchesRequest
import com.ballog.mobile.data.dto.DayMatchesResponse
import com.ballog.mobile.data.dto.MatchReportRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MatchApi {
    // 내 매치 리스트 조회
    @GET("v1/matches/me")
    suspend fun getMyMatches(
        @Header("Authorization") token: String,
        @Query("month") month: String
    ): Response<ApiResponse<MatchListResponse>>

    // 특정 팀의 매치 리스트 조회
    @GET("v1/matches/teams/{teamId}")
    suspend fun getTeamMatches(
        @Header("Authorization") token: String,
        @Path("teamId") teamId: Int,
        @Query("month") month: String
    ): Response<ApiResponse<MatchListResponse>>

    // 매치 등록
    @POST("v1/matches/me")
    suspend fun registerMyMatch(
        @Header("Authorization") token: String,
        @Body request: MatchRegisterRequest
    ): Response<ApiResponse<Unit>>

    // 팀 매치 등록
    @POST("v1/matches/teams")
    suspend fun registerTeamMatch(
        @Header("Authorization") token: String,
        @Body request: TeamMatchRegisterRequest
    ): Response<ApiResponse<Unit>>

    // 매치 상세 조회
    @GET("v1/matches/{matchId}")
    suspend fun getMatchDetail(
        @Header("Authorization") token: String,
        @Path("matchId") matchId: Int
    ): Response<ApiResponse<MatchDetailResponseDto>>

    // 매치 리포트 전송
    @POST("v1/quarter")
    suspend fun sendMatchReport(
        @Header("Authorization") token: String,
        @Body request: MatchReportRequest
    ): Response<ApiResponse<MatchReportResponse>>

    @POST("v1/matches/date")
    suspend fun getDayMatches(
        @Header ("Authorization") token: String,
        @Body request: DayMatchesRequest
    ): Response<ApiResponse<DayMatchesResponse>>

}
