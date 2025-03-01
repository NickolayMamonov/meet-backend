package dev.whysoezzy.data.model.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val phoneNumber = varchar("phone_number", 20).uniqueIndex()
    val name = varchar("name", 100)
    val surname = varchar("surname", 100).nullable()
    val socialLinks = text("social_links").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}