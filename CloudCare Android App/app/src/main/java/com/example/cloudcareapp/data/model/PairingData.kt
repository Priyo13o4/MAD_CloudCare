package com.example.cloudcareapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Pairing data from QR code scan
 */
data class PairingData(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("deviceName")
    val deviceName: String,
    
    @SerializedName("deviceType")
    val deviceType: String,
    
    @SerializedName("generatedAt")
    val generatedAt: String,
    
    @SerializedName("expiresAt")
    val expiresAt: String,
    
    @SerializedName("pairingCode")
    val pairingCode: String
)

/**
 * Pairing request to backend
 */
data class PairingRequest(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("deviceName")
    val deviceName: String,
    
    @SerializedName("deviceType")
    val deviceType: String,
    
    @SerializedName("generatedAt")
    val generatedAt: String,
    
    @SerializedName("expiresAt")
    val expiresAt: String,
    
    @SerializedName("pairingCode")
    val pairingCode: String,
    
    @SerializedName("androidUserId")
    val androidUserId: String
)

/**
 * Pairing response from backend
 */
data class PairingResponse(
    val message: String,
    
    @SerializedName("pairing_id")
    val pairingId: String,
    
    @SerializedName("ios_user_id")
    val iosUserId: String,
    
    @SerializedName("ios_device_id")
    val iosDeviceId: String,
    
    @SerializedName("android_user_id")
    val androidUserId: String,
    
    @SerializedName("paired_at")
    val pairedAt: String,
    
    @SerializedName("device_name")
    val deviceName: String
)
