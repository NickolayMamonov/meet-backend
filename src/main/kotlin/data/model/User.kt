package dev.whysoezzy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val phoneNumber: String,
    val name: String,
    val surname: String? = null,
    val socialLinks: Map<String, String> = emptyMap()
)

@Serializable
data class UserProfile(
    val id: String,
    val phoneNumber: String,
    val name: String,
    val surname: String? = null,
    val socialLinks: Map<String, String> = emptyMap(),
    val plannedMeetingsCount: Int = 0,
    val passedMeetingsCount: Int = 0,
    val communitiesCount: Int = 0
)

@Serializable
data class UserRequest(
    val name: String,
    val surname: String? = null,
    val socialLinks: Map<String, String>? = null
)

@Serializable
data class PhoneAuthRequest(
    val phoneNumber: String
)

@Serializable
data class OtpVerificationRequest(
    val phoneNumber: String,
    val otpCode: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val user: UserProfile
)