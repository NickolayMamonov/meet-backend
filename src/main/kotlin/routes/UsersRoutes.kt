package dev.whysoezzy.routes

import dev.whysoezzy.data.model.UserRequest
import dev.whysoezzy.services.UsersService
import dev.whysoezzy.utils.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.usersRoutes(usersService: UsersService) {

    route("/users") {
        // Protected routes
        authenticate {
            get("/profile") {
                val id = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val userProfile = usersService.getUserProfileById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(userProfile)
            }

            put("/profile") {
                val id = call.userId ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<UserRequest>()
                val updatedUser = usersService.updateUser(id, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound)
                call.respond(updatedUser)
            }

            delete {
                val id = call.userId ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val success = usersService.deleteUser(id)
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        // Admin routes would typically be here with additional authorization
    }
}