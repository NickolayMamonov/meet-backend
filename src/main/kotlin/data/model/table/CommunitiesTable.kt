package dev.whysoezzy.data.model.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object CommunitiesTable : Table("communities") {
    val id = varchar("id", 36)
    val title = varchar("title", 200)
    val description = text("description")
    val avatar = text("avatar")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}