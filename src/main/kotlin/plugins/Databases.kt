package dev.whysoezzy.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import dev.whysoezzy.data.model.table.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init(config: ApplicationConfig) {
        val dbConfig = config.config("database")

        val hikariConfig = HikariConfig().apply {
            driverClassName = dbConfig.property("driverClassName").getString()
            jdbcUrl = dbConfig.property("jdbcURL").getString()
            username = dbConfig.property("username").getString()
            password = dbConfig.property("password").getString()
            maximumPoolSize = dbConfig.property("maxPoolSize").getString().toInt()
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        try {
            val dataSource = HikariDataSource(hikariConfig)
            Database.connect(dataSource)

            // Create tables if they don't exist
            transaction {
                SchemaUtils.create(
                    UsersTable,
                    MeetingsTable,
                    CommunitiesTable,
                    UserMeetingsTable,
                    CommunityMembersTable,
                    CommunityMeetingsTable
                )
            }

            logger.info("Database initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize database", e)
            throw e
        }
    }
}