package dev.whysoezzy.data.repositories

import dev.whysoezzy.data.model.Meeting
import dev.whysoezzy.data.model.MeetingRequest
import dev.whysoezzy.data.model.table.CommunityMeetingsTable
import dev.whysoezzy.data.model.table.MeetingStatus
import dev.whysoezzy.data.model.table.MeetingsTable
import dev.whysoezzy.data.model.table.UserMeetingsTable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction

import java.time.LocalDateTime
import java.util.UUID

class MeetingsRepository {

    fun getAllMeetings(): List<Meeting> = transaction {
        MeetingsTable.selectAll()
            .orderBy(MeetingsTable.dateTime to SortOrder.DESC)
            .map { it.toMeeting() }
    }

    fun getActiveMeetings(): List<Meeting> = transaction {
        MeetingsTable.selectAll().where { MeetingsTable.isEnded eq false }
            .orderBy(MeetingsTable.dateTime to SortOrder.ASC)
            .map {
                it.toMeeting().copy(
                    participantsCount = getParticipantsCount(it[MeetingsTable.id])
                )
            }
    }

    fun getMeetingById(id: String): Meeting? = transaction {
        MeetingsTable.selectAll().where { MeetingsTable.id eq id }
            .singleOrNull()?.toMeeting()?.copy(
                participantsCount = getParticipantsCount(id)
            )
    }

    fun getMeetingsByIds(ids: List<String>): List<Meeting> = transaction {
        if (ids.isEmpty()) return@transaction emptyList()

        MeetingsTable.selectAll().where { MeetingsTable.id inList ids }
            .map { row ->
                row.toMeeting().copy(
                    participantsCount = getParticipantsCount(row[MeetingsTable.id])
                )
            }
    }

    fun getMeetingsByCommunityId(communityId: String): List<Meeting> = transaction {
        val meetingIds = CommunityMeetingsTable
            .selectAll().where { CommunityMeetingsTable.communityId eq communityId }
            .map { it[CommunityMeetingsTable.meetingId] }

        if (meetingIds.isEmpty()) return@transaction emptyList()

        MeetingsTable.selectAll().where { MeetingsTable.id inList meetingIds }
            .map { row ->
                row.toMeeting().copy(
                    participantsCount = getParticipantsCount(row[MeetingsTable.id])
                )
            }
    }

    fun createMeeting(request: MeetingRequest): Meeting = transaction {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()

        MeetingsTable.insert {
            it[MeetingsTable.id] = id
            it[title] = request.title
            it[description] = request.description
            it[location] = request.location
            it[dateTime] = LocalDateTime.parse(request.dateTime)
            it[isEnded] = false
            it[icon] = request.icon ?: ""
            it[images] = Json.encodeToString(request.images ?: emptyList<String>())
            it[tags] = Json.encodeToString(request.tags ?: emptyList<String>())
            it[createdAt] = now
            it[updatedAt] = now
        }

        MeetingsTable.selectAll().where { MeetingsTable.id eq id }
            .single()
            .toMeeting()
    }

    fun updateMeeting(id: String, request: MeetingRequest): Meeting? = transaction {
        val meetingExists = MeetingsTable.selectAll().where { MeetingsTable.id eq id }.singleOrNull() != null

        if (!meetingExists) return@transaction null

        MeetingsTable.update({ MeetingsTable.id eq id }) {
            it[title] = request.title
            it[description] = request.description
            it[location] = request.location
            it[dateTime] = LocalDateTime.parse(request.dateTime)
            it[icon] = request.icon ?: ""
            it[images] = Json.encodeToString(request.images ?: emptyList<String>())
            it[tags] = Json.encodeToString(request.tags ?: emptyList<String>())
            it[updatedAt] = LocalDateTime.now()
        }

        getMeetingById(id)
    }

    fun deleteMeeting(id: String): Boolean = transaction {
        // First delete all references in join tables
        UserMeetingsTable.deleteWhere { UserMeetingsTable.meetingId eq id }
        CommunityMeetingsTable.deleteWhere { CommunityMeetingsTable.meetingId eq id }

        // Then delete the meeting
        MeetingsTable.deleteWhere { MeetingsTable.id eq id } > 0
    }

    fun markMeetingAsEnded(id: String): Boolean = transaction {
        MeetingsTable.update({ MeetingsTable.id eq id }) {
            it[isEnded] = true
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    fun registerUserForMeeting(userId: String, meetingId: String): Boolean = transaction {
        val alreadyRegistered = UserMeetingsTable.selectAll()
            .where { (UserMeetingsTable.userId eq userId) and (UserMeetingsTable.meetingId eq meetingId) }.count() > 0

        if (alreadyRegistered) return@transaction false

        UserMeetingsTable.insert {
            it[UserMeetingsTable.userId] = userId
            it[UserMeetingsTable.meetingId] = meetingId
            it[status] = MeetingStatus.PLANNED
            it[registeredAt] = LocalDateTime.now()
        }

        return@transaction true
    }

    fun unregisterUserFromMeeting(userId: String, meetingId: String): Boolean = transaction {
        UserMeetingsTable.deleteWhere {
            (UserMeetingsTable.userId eq userId) and (UserMeetingsTable.meetingId eq meetingId)
        } > 0
    }

    fun isUserRegisteredForMeeting(userId: String, meetingId: String): Boolean = transaction {
        UserMeetingsTable.selectAll()
            .where { (UserMeetingsTable.userId eq userId) and (UserMeetingsTable.meetingId eq meetingId) }.count() > 0
    }

    fun getUserMeetings(userId: String, status: MeetingStatus?): List<Meeting> = transaction {
        val query = if (status != null) {
            UserMeetingsTable.selectAll()
                .where { (UserMeetingsTable.userId eq userId) and (UserMeetingsTable.status eq status) }
        } else {
            UserMeetingsTable.selectAll().where { UserMeetingsTable.userId eq userId }
        }

        val meetingIds = query.map { it[UserMeetingsTable.meetingId] }

        if (meetingIds.isEmpty()) return@transaction emptyList()

        MeetingsTable.selectAll().where { MeetingsTable.id inList meetingIds }
            .map { row ->
                row.toMeeting().copy(
                    participantsCount = getParticipantsCount(row[MeetingsTable.id])
                )
            }
    }

    private fun getParticipantsCount(meetingId: String): Int =
        UserMeetingsTable.selectAll().where { UserMeetingsTable.meetingId eq meetingId }.count().toInt()

    private fun ResultRow.toMeeting(): Meeting {
        return Meeting(
            id = this[MeetingsTable.id],
            title = this[MeetingsTable.title],
            description = this[MeetingsTable.description],
            location = this[MeetingsTable.location],
            dateTime = this[MeetingsTable.dateTime].toString(),
            isEnded = this[MeetingsTable.isEnded],
            icon = this[MeetingsTable.icon],
            images = Json.decodeFromString(this[MeetingsTable.images]),
            tags = Json.decodeFromString(this[MeetingsTable.tags]),
            participantsCount = 0 // This is filled later when needed
        )
    }
}
