package com.example.socialmediamobieapp.network

import com.example.socialmediamobieapp.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://tuandanh.xyz/api/v1.0.0/" // Dùng 10.0.2.2 cho emulator

    private lateinit var tokenManager: TokenManager

    // Gọi hàm này từ MainActivity để cung cấp TokenManager
    fun init(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    // Interceptor để thêm header Authorization
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // Lấy token từ TokenManager
        val token = tokenManager.getAccessToken()

        // Nếu token tồn tại, thêm header Authorization
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    // Interceptor để logging request và response
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Hiển thị toàn bộ request/response trong Logcat
    }

    // Cấu hình OkHttpClient với timeout và các interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Timeout kết nối: 30 giây
        .readTimeout(30, TimeUnit.SECONDS)    // Timeout đọc: 30 giây
        .writeTimeout(30, TimeUnit.SECONDS)   // Timeout ghi: 30 giây
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}