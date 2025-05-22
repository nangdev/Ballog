package com.ballog.mobile.data.api

import com.ballog.mobile.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // 인증이 필요없는 요청은 그대로 진행
        if (!requiresAuthentication(request)) {
            return chain.proceed(request)
        }

        // 액세스 토큰 가져오기
        val accessToken = runBlocking { tokenManager.getAccessTokenBlocking() }
        
        // 토큰이 없으면 원래 요청 진행
        if (accessToken == null) {
            return chain.proceed(request)
        }

        // 토큰을 헤더에 추가하여 요청
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        var response = chain.proceed(authenticatedRequest)

        // 401 에러 발생 시 토큰 갱신 시도
        if (response.code == 401) {
            response.close()
            
            try {
                // 토큰 갱신 시도
                val refreshResult = runBlocking { tokenManager.refreshTokens() }
                
                return when {
                    refreshResult.isSuccess -> {
                        // 새로운 토큰으로 요청 재시도
                        val newRequest = request.newBuilder()
                            .header("Authorization", "Bearer ${refreshResult.getOrNull()}")
                            .build()
                        chain.proceed(newRequest)
                    }
                    else -> {
                        // 토큰 갱신 실패 시 원래 응답 반환
                        response
                    }
                }
            } catch (e: IOException) {
                // 네트워크 오류 발생 시 원래 응답 반환
                return response
            }
        }

        return response
    }

    private fun requiresAuthentication(request: Request): Boolean {
        val path = request.url.encodedPath
        // 인증이 필요없는 엔드포인트 리스트
        return !path.contains("/v1/auth/login") &&
               !path.contains("/v1/auth/signup") &&
               !path.contains("/v1/auth/send-email") &&
               !path.contains("/v1/auth/verify-email")
    }
} 