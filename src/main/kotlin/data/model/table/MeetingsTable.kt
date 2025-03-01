package dev.whysoezzy.data.model.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object MeetingsTable : Table("meetings") {
    val id = varchar("id", 36)
    val title = varchar("title", 200)
    val description = text("description")
    val location = varchar("location", 200)
    val dateTime = datetime("date_time")
    val isEnded = bool("is_ended")
    val icon = text("icon")
    val images = text("images")
    val tags = text("tags")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}