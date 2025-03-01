package dev.whysoezzy.data.model

import kotlinx.serialization.Serializable


@Serializable
data class Meeting(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val dateTime: String, // ISO-8601 format
    val isEnded: Boolean,
    val icon: String,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val participantsCount: Int = 0
)

@Serializable
data class MeetingRequest(
    val title: String,
    val description: String,
    val location: String,
    val dateTime: String, // ISO-8601 format
    val icon: String? = null,
    val images: List<String>? = null,
    val tags: List<String>? = null
)

@Serializable
data class MeetingRegistrationResponse(
    val meetingId: String,
    val userId: String,
    val registered: Boolean,
    val message: String
)