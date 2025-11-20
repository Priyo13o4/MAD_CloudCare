package com.example.cloudcareapp.data.remote

import com.example.cloudcareapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    
    // Patient Signup
    @POST("auth/signup/patient")
    suspend fun registerPatient(
        @Body request: RegisterPatientRequest
    ): Response<TokenResponse>
    
    // Doctor Signup
    @POST("auth/signup/doctor")
    suspend fun registerDoctor(
        @Body request: RegisterDoctorRequest
    ): Response<TokenResponse>
    
    // Hospital Signup
    @POST("auth/signup/hospital")
    suspend fun registerHospital(
        @Body request: RegisterHospitalRequest
    ): Response<TokenResponse>
    
    // Login (all roles)
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>
    
    // Refresh Access Token
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<TokenResponse>
    
    // Get Current User Info
    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<AuthUserResponse>
}
