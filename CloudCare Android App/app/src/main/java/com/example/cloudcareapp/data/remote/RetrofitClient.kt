package com.example.cloudcareapp.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit HTTP Client for CloudCare Backend
 * 
 * Provides singleton access to the API service with proper configuration:
 * - Base URL: https://cloudcare.pipfactor.com/api/v1/
 * - JSON serialization via Gson
 * - Request/response logging for debugging
 * - Timeouts configured for mobile network conditions
 */
object RetrofitClient {
    
    private const val BASE_URL = "https://cloudcare.pipfactor.com/api/v1/"
    
    /**
     * HTTP logging interceptor for debugging
     * Logs request/response details to Logcat
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * OkHttp client with logging and timeout configuration
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Lazy-initialized Retrofit instance
     * 
     * Only created when first accessed, then reused for all subsequent calls
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * API service instance
     * 
     * Use this to make API calls throughout the app:
     * ```kotlin
     * val summary = RetrofitClient.apiService.getTodaySummary(patientId)
     * ```
     */
    val apiService: CloudCareApiService by lazy {
        retrofit.create(CloudCareApiService::class.java)
    }
    
    /**
     * Authentication API service instance
     * 
     * Use this for authentication operations:
     * ```kotlin
     * val response = RetrofitClient.authApiService.login(loginRequest)
     * ```
     */
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}
