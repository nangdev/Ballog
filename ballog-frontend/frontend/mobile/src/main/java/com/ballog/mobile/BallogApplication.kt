package com.ballog.mobile

import android.app.Application
import com.ballog.mobile.data.api.RetrofitInstance
import com.ballog.mobile.data.local.TokenManager
import com.ballog.mobile.util.S3Utils

class BallogApplication : Application() {
    private var _tokenManager: TokenManager? = null
    val tokenManager: TokenManager
        get() = _tokenManager ?: throw IllegalStateException("TokenManager not initialized")

    override fun onCreate() {
        super.onCreate()
        
        // RetrofitInstance 초기화
        RetrofitInstance.init(this)
        // TokenManager는 RetrofitInstance 초기화 후에 자동으로 생성됨
        _tokenManager = RetrofitInstance.getTokenManager()
        // S3Utils 초기화 (앱 시작 시 1회만)
        S3Utils.init(this)
    }

    companion object {
        private lateinit var instance: BallogApplication
        
        fun getInstance(): BallogApplication {
            return instance
        }
    }

    init {
        instance = this
    }
} 