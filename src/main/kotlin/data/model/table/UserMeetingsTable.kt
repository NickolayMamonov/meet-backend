package dev.whysoezzy.data.model.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UserMeetingsTable : Table("user_meetings") {
    val userId = varchar("user_id", 36).references(UsersTable.id)
    val meetingId = varchar("meeting_id", 36).references(MeetingsTable.id)
    val status = enumeration<MeetingStatus>("status")
    val registeredAt = datetime("registered_at")

    override val primaryKey = PrimaryKey(userId, meetingId)
}