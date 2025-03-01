package dev.whysoezzy.data.repositories

import dev.whysoezzy.data.model.Community
import dev.whysoezzy.data.model.CommunityRequest
import dev.whysoezzy.data.model.table.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction

import java.time.LocalDateTime
import java.util.UUID

class CommunitiesRepository {

    fun getAllCommunities(): List<Community> = transaction {
        CommunitiesTable.selectAll()
            .orderBy(CommunitiesTable.title to SortOrder.ASC)
            .map { row ->
                row.toCommunity().copy(
                    memberCount = getMemberCount(row[CommunitiesTable.id]),
                    meetingsCount = getMeetingsCount(row[CommunitiesTable.id])
                )
            }
    }

    fun getCommunityById(id: String): Community? = transaction {
        CommunitiesTable.selectAll().where { CommunitiesTable.id eq id }
            .singleOrNull()?.let { row ->
                row.toCommunity().copy(
                    memberCount = getMemberCount(id),
                    meetingsCount = getMeetingsCount(id)
                )
            }
    }

    fun getCommunityByTitle(title: String): Community? = transaction {
        CommunitiesTable.selectAll().where { CommunitiesTable.title eq title }
            .singleOrNull()?.let { row ->
                row.toCommunity().copy(
                    memberCount = getMemberCount(row[CommunitiesTable.id]),
                    meetingsCount = getMeetingsCount(row[CommunitiesTable.id])
                )
            }
    }

    fun createCommunity(request: CommunityRequest): Community = transaction {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()

        CommunitiesTable.insert {
            it[CommunitiesTable.id] = id
            it[title] = request.title
            it[description] = request.description
            it[avatar] = request.avatar ?: ""
            it[createdAt] = now
            it[updatedAt] = now
        }

        CommunitiesTable.selectAll().where { CommunitiesTable.id eq id }
            .single()
            .toCommunity()
    }

    fun updateCommunity(id: String, request: CommunityRequest): Community? = transaction {
        val communityExists = CommunitiesTable.selectAll().where { CommunitiesTable.id eq id }.singleOrNull() != null

        if (!communityExists) return@transaction null

        CommunitiesTable.update({ CommunitiesTable.id eq id }) {
            it[title] = request.title
            it[description] = request.description
            if (request.avatar != null) {
                it[avatar] = request.avatar
            }
            it[updatedAt] = LocalDateTime.now()
        }

        getCommunityById(id)
    }

    fun deleteCommunity(id: String): Boolean = transaction {
        // First delete all references in join tables
        CommunityMembersTable.deleteWhere { CommunityMembersTable.communityId eq id }
        CommunityMeetingsTable.deleteWhere { CommunityMeetingsTable.communityId eq id }

        // Then delete the community
        CommunitiesTable.deleteWhere { CommunitiesTable.id eq id } > 0
    }

    fun addMemberToCommunity(communityId: String, userId: String): Boolean = transaction {
        val alreadyMember = CommunityMembersTable.selectAll()
            .where { (CommunityMembersTable.communityId eq communityId) and (CommunityMembersTable.userId eq userId) }
            .count() > 0

        if (alreadyMember) return@transaction false

        CommunityMembersTable.insert {
            it[CommunityMembersTable.communityId] = communityId
            it[CommunityMembersTable.userId] = userId
            it[joinedAt] = LocalDateTime.now()
        }

        return@transaction true
    }

    fun removeMemberFromCommunity(communityId: String, userId: String): Boolean = transaction {
        CommunityMembersTable.deleteWhere {
            (CommunityMembersTable.communityId eq communityId) and (CommunityMembersTable.userId eq userId)
        } > 0
    }

    fun isUserMemberOfCommunity(communityId: String, userId: String): Boolean = transaction {
        CommunityMembersTable.selectAll()
            .where { (CommunityMembersTable.communityId eq communityId) and (CommunityMembersTable.userId eq userId) }
            .count() > 0
    }

    fun addMeetingToCommunity(communityId: String, meetingId: String): Boolean = transaction {
        val alreadyAdded = CommunityMeetingsTable.selectAll()
            .where { (CommunityMeetingsTable.communityId eq communityId) and (CommunityMeetingsTable.meetingId eq meetingId) }
            .count() > 0

        if (alreadyAdded) return@transaction false

        CommunityMeetingsTable.insert {
            it[CommunityMeetingsTable.communityId] = communityId
            it[CommunityMeetingsTable.meetingId] = meetingId
            it[addedAt] = LocalDateTime.now()
        }

        return@transaction true
    }

    fun removeMeetingFromCommunity(communityId: String, meetingId: String): Boolean = transaction {
        CommunityMeetingsTable.deleteWhere {
            (CommunityMeetingsTable.communityId eq communityId) and (CommunityMeetingsTable.meetingId eq meetingId)
        } > 0
    }

    fun getUserCommunities(userId: String): List<Community> = transaction {
        val communityIds = CommunityMembersTable
            .selectAll().where { CommunityMembersTable.userId eq userId }
            .map { it[CommunityMembersTable.communityId] }

        if (communityIds.isEmpty()) return@transaction emptyList()

        CommunitiesTable.selectAll().where { CommunitiesTable.id inList communityIds }
            .map { row ->
                row.toCommunity().copy(
                    memberCount = getMemberCount(row[CommunitiesTable.id]),
                    meetingsCount = getMeetingsCount(row[CommunitiesTable.id])
                )
            }
    }

    private fun getMemberCount(communityId: String): Int =
        CommunityMembersTable.selectAll().where { CommunityMembersTable.communityId eq communityId }.count().toInt()

    private fun getMeetingsCount(communityId: String): Int =
        CommunityMeetingsTable.selectAll().where { CommunityMeetingsTable.communityId eq communityId }.count().toInt()

    private fun ResultRow.toCommunity(): Community {
        return Community(
            id = this[CommunitiesTable.id],
            title = this[CommunitiesTable.title],
            description = this[CommunitiesTable.description],
            avatar = this[CommunitiesTable.avatar],
            memberCount = 0, // This is filled later when needed
            meetingsCount = 0 // This is filled later when needed
        )
    }
}