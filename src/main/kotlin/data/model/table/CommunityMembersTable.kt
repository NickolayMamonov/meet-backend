package dev.whysoezzy.data.model.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CommunityMembersTable : Table("community_members") {
    val communityId = varchar("community_id", 36).references(CommunitiesTable.id)
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val joinedAt = datetime("joined_at")

    override val primaryKey = PrimaryKey(communityId, userId)
}