package com.ballog.mobile.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ballog.mobile.data.api.AuthApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference

class TokenManager(
    context: Context,
    private val authApi: AuthApi
) {
    private val contextRef = WeakReference(context.applicationContext)

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token_prefs")
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

        // 싱글톤 인스턴스
        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context, authApi: AuthApi): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext, authApi).also { instance = it }
            }
        }
    }

    private val mutex = Mutex()

    private fun getContext(): Context {
        return contextRef.get() ?: throw IllegalStateException("Context is no longer available")
    }

    // 토큰 저장
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        getContext().dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    // 액세스 토큰 가져오기
    fun getAccessToken(): Flow<String?> {
        return getContext().dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }
    }

    // 리프레시 토큰 가져오기
    fun getRefreshToken(): Flow<String?> {
        return getContext().dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }
    }

    // 토큰 삭제 (로그아웃 시)
    suspend fun clearTokens() {
        println("TokenManager - Starting token clearing process")
        try {
            getContext().dataStore.edit { preferences ->
                preferences.clear()  // 모든 데이터 삭제
            }
            // 토큰이 실제로 삭제되었는지 확인
            val hasTokens = hasTokens().first()
            println("TokenManager - Token clearing completed, hasTokens: $hasTokens")
        } catch (e: Exception) {
            println("TokenManager - Error clearing tokens: ${e.message}")
            throw e
        }
    }

    // 토큰 존재 여부 확인
    fun hasTokens(): Flow<Boolean> {
        return getContext().dataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN]
            val refreshToken = preferences[REFRESH_TOKEN]
            println("TokenManager - Checking tokens: accessToken=${accessToken != null}, refreshToken=${refreshToken != null}")
            accessToken != null && refreshToken != null
        }
    }

    // 토큰 갱신
    suspend fun refreshTokens(): Result<String> = mutex.withLock {
        try {
            val refreshToken = getRefreshToken().first() ?: throw IllegalStateException("Refresh token not found")
            val response = authApi.refreshToken("Bearer $refreshToken")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.isSuccess == true) {
                    apiResponse.result?.let { tokenResponse ->
                        saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                        return Result.success(tokenResponse.accessToken)
                    } ?: return Result.failure(Exception("Token result is null"))
                } else {
                    return Result.failure(Exception("Token refresh failed: ${apiResponse?.message}"))
                }
            }
            
            // 토큰 갱신 실패 시 토큰 삭제
            clearTokens()
            Result.failure(Exception("Token refresh failed: ${response.code()}"))
        } catch (e: Exception) {
            clearTokens()
            Result.failure(e)
        }
    }

    // 액세스 토큰 가져오기 (동기 방식)
    suspend fun getAccessTokenBlocking(): String? {
        return getAccessToken().first()
    }
} 
