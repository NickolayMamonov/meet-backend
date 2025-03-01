package dev.whysoezzy.plugins

import dev.whysoezzy.services.MeetingsService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.wb.meetings.routes.*
import ru.wb.meetings.services.*

fun Application.configureRouting() {
    val meetingsService by inject<MeetingsService>()
    val communitiesService by inject<CommunitiesService>()
    val usersService by inject<UsersService>()
    val authService by inject<AuthService>()

    routing {
        // Public auth routes
        authRoutes(authService)

        // API routes
        route("/api") {
            meetingsRoutes(meetingsService)
            communitiesRoutes(communitiesService)
            usersRoutes(usersService)
        }
    }
}