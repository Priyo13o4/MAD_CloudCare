package com.example.cloudcareapp.data.model

import com.google.gson.annotations.SerializedName

// User roles
enum class UserRole {
    @SerializedName("PATIENT") PATIENT,
    @SerializedName("DOCTOR") DOCTOR,
    @SerializedName("HOSPITAL_ADMIN") HOSPITAL_ADMIN
}

// Gender enum
enum class Gender {
    @SerializedName("MALE") MALE,
    @SerializedName("FEMALE") FEMALE,
    @SerializedName("OTHER") OTHER
}

// Blood group enum
enum class BloodGroup {
    @SerializedName("A+") A_POSITIVE,
    @SerializedName("A-") A_NEGATIVE,
    @SerializedName("B+") B_POSITIVE,
    @SerializedName("B-") B_NEGATIVE,
    @SerializedName("AB+") AB_POSITIVE,
    @SerializedName("AB-") AB_NEGATIVE,
    @SerializedName("O+") O_POSITIVE,
    @SerializedName("O-") O_NEGATIVE
}

// Facility type enum
enum class FacilityType {
    @SerializedName("MULTI_SPECIALTY_HOSPITAL") MULTI_SPECIALTY_HOSPITAL,
    @SerializedName("SUPER_SPECIALTY_HOSPITAL") SUPER_SPECIALTY_HOSPITAL,
    @SerializedName("GENERAL_HOSPITAL") GENERAL_HOSPITAL,
    @SerializedName("CLINIC") CLINIC,
    @SerializedName("DIAGNOSTIC_CENTER") DIAGNOSTIC_CENTER,
    @SerializedName("NURSING_HOME") NURSING_HOME
}

// ==================== Request Models ====================

// Patient Registration Request
data class RegisterPatientRequest(
    val email: String,
    val password: String,
    @SerializedName("aadhar_number") val aadharNumber: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("date_of_birth") val dateOfBirth: String, // Format: YYYY-MM-DD
    val gender: Gender,
    @SerializedName("blood_group") val bloodGroup: BloodGroup? = null,
    @SerializedName("phone_primary") val phonePrimary: String,
    @SerializedName("phone_secondary") val phoneSecondary: String? = null,
    @SerializedName("address_line1") val addressLine1: String,
    @SerializedName("address_line2") val addressLine2: String? = null,
    val city: String,
    val state: String,
    @SerializedName("postal_code") val postalCode: String,
    val country: String = "India",
    @SerializedName("emergency_contact_name") val emergencyContactName: String,
    @SerializedName("emergency_contact_phone") val emergencyContactPhone: String,
    @SerializedName("emergency_contact_relation") val emergencyContactRelation: String,
    @SerializedName("height_cm") val heightCm: Double? = null,
    @SerializedName("weight_kg") val weightKg: Double? = null,
    val allergies: List<String>? = null,
    @SerializedName("chronic_conditions") val chronicConditions: List<String>? = null,
    @SerializedName("current_medications") val currentMedications: List<String>? = null,
    @SerializedName("insurance_provider") val insuranceProvider: String? = null,
    @SerializedName("insurance_policy_no") val insurancePolicyNo: String? = null
)

// Doctor Registration Request
data class RegisterDoctorRequest(
    val email: String,
    val password: String,
    @SerializedName("medical_license_no") val medicalLicenseNo: String,
    @SerializedName("registration_year") val registrationYear: Int,
    @SerializedName("registration_state") val registrationState: String,
    val title: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("middle_name") val middleName: String? = null,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("phone_primary") val phonePrimary: String,
    @SerializedName("phone_secondary") val phoneSecondary: String? = null,
    val specialization: String,
    val qualifications: List<String>,
    @SerializedName("experience_years") val experienceYears: Int,
    @SerializedName("hospital_code") val hospitalCode: String,
    @SerializedName("consultation_fee") val consultationFee: Double? = null,
    @SerializedName("available_for_emergency") val availableForEmergency: Boolean = false,
    @SerializedName("telemedicine_enabled") val telemedicineEnabled: Boolean = false,
    val languages: List<String>? = null,
    @SerializedName("clinic_address") val clinicAddress: String? = null,
    @SerializedName("bio") val bio: String? = null
)

// Hospital Registration Request
data class RegisterHospitalRequest(
    val email: String,
    val password: String,
    val name: String,
    @SerializedName("phone_primary") val phonePrimary: String,
    @SerializedName("phone_secondary") val phoneSecondary: String? = null,
    @SerializedName("phone_emergency") val phoneEmergency: String? = null,
    @SerializedName("address_line1") val addressLine1: String,
    @SerializedName("address_line2") val addressLine2: String? = null,
    val city: String,
    val state: String,
    @SerializedName("postal_code") val postalCode: String,
    val country: String = "India",
    @SerializedName("total_beds") val totalBeds: Int,
    @SerializedName("icu_beds") val icuBeds: Int,
    @SerializedName("emergency_beds") val emergencyBeds: Int,
    @SerializedName("operation_theatres") val operationTheatres: Int,
    @SerializedName("has_emergency") val hasEmergency: Boolean = true,
    @SerializedName("has_ambulance") val hasAmbulance: Boolean = false,
    @SerializedName("has_pharmacy") val hasPharmacy: Boolean = false,
    @SerializedName("has_lab") val hasLab: Boolean = false,
    @SerializedName("has_blood_bank") val hasBloodBank: Boolean = false,
    @SerializedName("facility_type") val facilityType: FacilityType,
    val specializations: List<String>? = null,
    val accreditations: List<String>? = null
)

// Login Request
data class LoginRequest(
    val email: String? = null,
    val aadhar: String? = null,
    val password: String
)

// Refresh Token Request
data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

// ==================== Response Models ====================

// Auth User Data (returned in token response)
data class AuthUserResponse(
    val id: String,
    val email: String,
    val role: UserRole,
    val name: String? = null,
    @SerializedName("patient_id") val patientId: String? = null,
    @SerializedName("doctor_id") val doctorId: String? = null,
    @SerializedName("hospital_id") val hospitalId: String? = null,
    @SerializedName("aadhar_uid") val aadharUid: String? = null,
    @SerializedName("hospital_code") val hospitalCode: String? = null
)

// Token Response (signup & login)
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: AuthUserResponse
)

// Error Response
data class ErrorResponse(
    val detail: String,
    val message: String? = null
)

// ==================== Local Storage Models ====================

// User session data stored locally
data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUserResponse
) {
    fun isPatient() = user.role == UserRole.PATIENT
    fun isDoctor() = user.role == UserRole.DOCTOR
    fun isHospitalAdmin() = user.role == UserRole.HOSPITAL_ADMIN
}
