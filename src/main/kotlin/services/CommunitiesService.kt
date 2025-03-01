package dev.whysoezzy.services

import dev.whysoezzy.data.model.Community
import dev.whysoezzy.data.model.CommunityMembershipResponse
import dev.whysoezzy.data.model.CommunityRequest
import dev.whysoezzy.data.model.Meeting
import dev.whysoezzy.data.repositories.CommunitiesRepository
import dev.whysoezzy.data.repositories.MeetingsRepository

class CommunitiesService(
    private val communitiesRepository: CommunitiesRepository,
    private val meetingsRepository: MeetingsRepository
) {

    fun getAllCommunities(): List<Community> {
        return communitiesRepository.getAllCommunities()
    }

    fun getCommunityById(id: String): Community? {
        return communitiesRepository.getCommunityById(id)
    }

    fun getCommunityByTitle(title: String): Community? {
        return communitiesRepository.getCommunityByTitle(title)
    }

    fun createCommunity(request: CommunityRequest): Community {
        return communitiesRepository.createCommunity(request)
    }

    fun updateCommunity(id: String, request: CommunityRequest): Community? {
        return communitiesRepository.updateCommunity(id, request)
    }

    fun deleteCommunity(id: String): Boolean {
        return communitiesRepository.deleteCommunity(id)
    }

    fun getCommunityMeetings(communityId: String): List<Meeting> {
        return meetingsRepository.getMeetingsByCommunityId(communityId)
    }

    fun addMemberToCommunity(communityId: String, userId: String): CommunityMembershipResponse {
        val community = communitiesRepository.getCommunityById(communityId)
        if (community == null) {
            return CommunityMembershipResponse(communityId, userId, false, "Community not found")
        }

        val isAdded = communitiesRepository.addMemberToCommunity(communityId, userId)
        return if (isAdded) {
            CommunityMembershipResponse(communityId, userId, true, "Successfully joined community")
        } else {
            CommunityMembershipResponse(communityId, userId, true, "User is already a member of this community")
        }
    }

    fun removeMemberFromCommunity(communityId: String, userId: String): CommunityMembershipResponse {
        val isRemoved = communitiesRepository.removeMemberFromCommunity(communityId, userId)
        return if (isRemoved) {
            CommunityMembershipResponse(communityId, userId, false, "Successfully left community")
        } else {
            CommunityMembershipResponse(communityId, userId, false, "User is not a member of this community")
        }
    }

    fun isUserMemberOfCommunity(communityId: String, userId: String): Boolean {
        return communitiesRepository.isUserMemberOfCommunity(communityId, userId)
    }

    fun addMeetingToCommunity(communityId: String, meetingId: String): Boolean {
        return communitiesRepository.addMeetingToCommunity(communityId, meetingId)
    }

    fun removeMeetingFromCommunity(communityId: String, meetingId: String): Boolean {
        return communitiesRepository.removeMeetingFromCommunity(communityId, meetingId)
    }

    fun getUserCommunities(userId: String): List<Community> {
        return communitiesRepository.getUserCommunities(userId)
    }
}