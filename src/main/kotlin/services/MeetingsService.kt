package dev.whysoezzy.services

import dev.whysoezzy.data.model.Meeting
import dev.whysoezzy.data.model.MeetingRegistrationResponse
import dev.whysoezzy.data.model.MeetingRequest
import dev.whysoezzy.data.model.table.MeetingStatus
import dev.whysoezzy.data.repositories.MeetingsRepository
import dev.whysoezzy.data.repositories.UsersRepository


class MeetingsService(
    private val meetingsRepository: MeetingsRepository,
    private val usersRepository: UsersRepository
) {

    fun getAllMeetings(): List<Meeting> {
        return meetingsRepository.getAllMeetings()
    }

    fun getActiveMeetings(): List<Meeting> {
        return meetingsRepository.getActiveMeetings()
    }

    fun getMeetingById(id: String): Meeting? {
        return meetingsRepository.getMeetingById(id)
    }

    fun getMeetingsByIds(ids: List<String>): List<Meeting> {
        return meetingsRepository.getMeetingsByIds(ids)
    }

    fun getMeetingsByCommunity(communityId: String): List<Meeting> {
        return meetingsRepository.getMeetingsByCommunityId(communityId)
    }

    fun getUserPlannedMeetings(userId: String): List<Meeting> {
        return meetingsRepository.getUserMeetings(userId, MeetingStatus.PLANNED)
    }

    fun getUserPassedMeetings(userId: String): List<Meeting> {
        return meetingsRepository.getUserMeetings(userId, MeetingStatus.PASSED)
    }

    fun createMeeting(request: MeetingRequest): Meeting {
        return meetingsRepository.createMeeting(request)
    }

    fun updateMeeting(id: String, request: MeetingRequest): Meeting? {
        return meetingsRepository.updateMeeting(id, request)
    }

    fun deleteMeeting(id: String): Boolean {
        return meetingsRepository.deleteMeeting(id)
    }

    fun registerUserForMeeting(userId: String, meetingId: String): MeetingRegistrationResponse {
        val user = usersRepository.getUserById(userId)
        if (user == null) {
            return MeetingRegistrationResponse(meetingId, userId, false, "User not found")
        }

        val meeting = meetingsRepository.getMeetingById(meetingId)
        if (meeting == null) {
            return MeetingRegistrationResponse(meetingId, userId, false, "Meeting not found")
        }

        if (meeting.isEnded) {
            return MeetingRegistrationResponse(meetingId, userId, false, "Meeting has already ended")
        }

        val isRegistered = meetingsRepository.registerUserForMeeting(userId, meetingId)
        return if (isRegistered) {
            MeetingRegistrationResponse(meetingId, userId, true, "Successfully registered for meeting")
        } else {
            MeetingRegistrationResponse(meetingId, userId, false, "User is already registered for this meeting")
        }
    }

    fun unregisterUserFromMeeting(userId: String, meetingId: String): MeetingRegistrationResponse {
        val isUnregistered = meetingsRepository.unregisterUserFromMeeting(userId, meetingId)
        return if (isUnregistered) {
            MeetingRegistrationResponse(meetingId, userId, false, "Successfully unregistered from meeting")
        } else {
            MeetingRegistrationResponse(meetingId, userId, true, "User was not registered for this meeting")
        }
    }

    fun isUserRegisteredForMeeting(userId: String, meetingId: String): Boolean {
        return meetingsRepository.isUserRegisteredForMeeting(userId, meetingId)
    }

    fun markMeetingAsEnded(id: String): Boolean {
        return meetingsRepository.markMeetingAsEnded(id)
    }
}