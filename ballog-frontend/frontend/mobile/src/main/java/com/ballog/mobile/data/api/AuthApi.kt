package com.ballog.mobile.data.api

import com.ballog.mobile.data.dto.EmailSendRequest
import com.ballog.mobile.data.dto.EmailVerifyRequest
import com.ballog.mobile.data.dto.LoginRequest
import com.ballog.mobile.data.dto.LoginResponse
import com.ballog.mobile.data.dto.RefreshTokenResponse
import com.ballog.mobile.data.dto.SignUpRequest
import com.ballog.mobile.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    @POST("v1/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>

    @POST("v1/auth/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<ApiResponse<Unit>>

    @POST("v1/auth/signout")
    suspend fun signOut(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>

    @POST("v1/auth/send-email")
    suspend fun sendEmail(
        @Body request: EmailSendRequest
    ): Response<ApiResponse<Unit>>

    @POST("v1/auth/verify-email")
    suspend fun verifyEmail(
        @Body request: EmailVerifyRequest
    ): Response<ApiResponse<Unit>>

    @POST("v1/auth/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") token: String
    ): Response<ApiResponse<RefreshTokenResponse>>
}
