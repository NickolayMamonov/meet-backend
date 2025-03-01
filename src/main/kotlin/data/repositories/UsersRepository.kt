package dev.whysoezzy.data.repositories

import dev.whysoezzy.data.model.User
import dev.whysoezzy.data.model.UserProfile
import dev.whysoezzy.data.model.UserRequest
import dev.whysoezzy.data.model.table.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

import java.time.LocalDateTime
import java.util.UUID

class UsersRepository {

    fun getUserById(id: String): User? = transaction {
        UsersTable.selectAll().where { UsersTable.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    fun getUserProfileById(id: String): UserProfile? = transaction {
        val user = getUserById(id) ?: return@transaction null

        val plannedMeetingsCount = UserMeetingsTable.selectAll()
            .where { (UserMeetingsTable.userId eq id) and (UserMeetingsTable.status eq MeetingStatus.PLANNED) }.count()
            .toInt()

        val passedMeetingsCount = UserMeetingsTable.selectAll()
            .where { (UserMeetingsTable.userId eq id) and (UserMeetingsTable.status eq MeetingStatus.PASSED) }.count()
            .toInt()

        val communitiesCount =
            CommunityMembersTable.selectAll().where { CommunityMembersTable.userId eq id }.count().toInt()

        UserProfile(
            id = user.id,
            phoneNumber = user.phoneNumber,
            name = user.name,
            surname = user.surname,
            socialLinks = user.socialLinks,
            plannedMeetingsCount = plannedMeetingsCount,
            passedMeetingsCount = passedMeetingsCount,
            communitiesCount = communitiesCount
        )
    }

    fun getUserByPhone(phoneNumber: String): User? = transaction {
        UsersTable.selectAll().where { UsersTable.phoneNumber eq phoneNumber }
            .singleOrNull()
            ?.toUser()
    }

    fun createUser(phoneNumber: String, request: UserRequest): User = transaction {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()

        UsersTable.insert {
            it[UsersTable.id] = id
            it[UsersTable.phoneNumber] = phoneNumber
            it[name] = request.name
            it[surname] = request.surname
            it[socialLinks] = request.socialLinks?.let { links -> Json.encodeToString(links) }
            it[createdAt] = now
            it[updatedAt] = now
        }

        UsersTable.selectAll().where { UsersTable.id eq id }
            .single()
            .toUser()
    }

    fun updateUser(id: String, request: UserRequest): User? = transaction {
        val userExists = UsersTable.selectAll().where { UsersTable.id eq id }.singleOrNull() != null

        if (!userExists) return@transaction null

        UsersTable.update({ UsersTable.id eq id }) {
            it[name] = request.name
            it[surname] = request.surname
            if (request.socialLinks != null) {
                it[socialLinks] = Json.encodeToString(request.socialLinks)
            }
            it[updatedAt] = LocalDateTime.now()
        }

        getUserById(id)
    }

    fun deleteUser(id: String): Boolean = transaction {
        // First delete all references in join tables
        UserMeetingsTable.deleteWhere { UserMeetingsTable.userId eq id }
        CommunityMembersTable.deleteWhere { CommunityMembersTable.userId eq id }

        // Then delete the user
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }

    private fun ResultRow.toUser(): User {
        val socialLinksStr = this[UsersTable.socialLinks]
        val links = if (socialLinksStr != null) {
            Json.decodeFromString<Map<String, String>>(socialLinksStr)
        } else {
            emptyMap()
        }

        return User(
            id = this[UsersTable.id],
            phoneNumber = this[UsersTable.phoneNumber],
            name = this[UsersTable.name],
            surname = this[UsersTable.surname],
            socialLinks = links
        )
    }
}