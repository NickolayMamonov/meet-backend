package dev.whysoezzy.data.model

import kotlinx.serialization.Serializable


@Serializable
data class Community(
    val id: String,
    val title: String,
    val description: String,
    val avatar: String,
    val memberCount: Int = 0,
    val meetingsCount: Int = 0
)

@Serializable
data class CommunityRequest(
    val title: String,
    val description: String,
    val avatar: String? = null
)

@Serializable
data class CommunityMembershipResponse(
    val communityId: String,
    val userId: String,
    val isMember: Boolean,
    val message: String
)