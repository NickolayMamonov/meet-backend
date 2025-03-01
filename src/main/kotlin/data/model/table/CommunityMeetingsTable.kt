package dev.whysoezzy.data.model.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CommunityMeetingsTable : Table("community_meetings") {
    val communityId = varchar("community_id", 36).references(CommunitiesTable.id)
    val meetingId = varchar("meeting_id", 36).references(MeetingsTable.id)
    val addedAt = datetime("added_at")

    override val primaryKey = PrimaryKey(communityId, meetingId)
}