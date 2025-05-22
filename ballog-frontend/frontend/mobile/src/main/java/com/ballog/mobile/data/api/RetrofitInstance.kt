package com.ballog.mobile.data.api

import android.content.Context
import com.ballog.mobile.data.local.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log

object RetrofitInstance {
    private const val BASE_URL = "https://k12a404.p.ssafy.io/api/"

    private var retrofit: Retrofit? = null
    private var tokenManager: TokenManager? = null

    fun init(context: Context) {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    val tempRetrofit = createRetrofit()
                    val tempAuthApi = tempRetrofit.create(AuthApi::class.java)
                    tokenManager = TokenManager.getInstance(context, tempAuthApi)

                    val okHttpClient = createOkHttpClient(tokenManager!!)

                    retrofit = createRetrofit(okHttpClient)
                }
            }
        }
    }

    fun getTokenManager(): TokenManager {
        return tokenManager ?: throw IllegalStateException("RetrofitInstance must be initialized before use")
    }

    private fun createOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    private fun createRetrofit(okHttpClient: OkHttpClient? = null): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .apply {
                okHttpClient?.let { client(it) }
            }
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit?.create(AuthApi::class.java) ?: throw IllegalStateException("RetrofitInstance must be initialized before use")
    }

    val teamApi: TeamApi by lazy {
        retrofit?.create(TeamApi::class.java) ?: throw IllegalStateException("RetrofitInstance must be initialized before use")
    }

    val matchApi: MatchApi by lazy {
        retrofit?.create(MatchApi::class.java) ?: throw IllegalStateException("RetrofitInstance must be initialized before use")
    }

    val userApi: UserApi by lazy {
        retrofit?.create(UserApi::class.java) ?: throw IllegalStateException("RetrofitInstance must be initialized before use")
    }

    val videoApi: VideoApi by lazy {
        retrofit?.create(VideoApi::class.java) ?: throw IllegalStateException("RetrofitInstance must be initialized before use")
    }
} 
