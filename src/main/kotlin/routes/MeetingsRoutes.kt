package dev.whysoezzy.routes

import dev.whysoezzy.data.model.MeetingRequest
import dev.whysoezzy.services.MeetingsService
import dev.whysoezzy.utils.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.meetingsRoutes(meetingsService: MeetingsService) {

    route("/meetings") {
        // Public routes
        get {
            call.respond(meetingsService.getAllMeetings())
        }

        get("/active") {
            call.respond(meetingsService.getActiveMeetings())
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val meeting = meetingsService.getMeetingById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(meeting)
        }

        // Protected routes
        authenticate {
            post {
                val request = call.receive<MeetingRequest>()
                val meeting = meetingsService.createMeeting(request)
                call.respond(HttpStatusCode.Created, meeting)
            }

            put("/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<MeetingRequest>()
                val updatedMeeting = meetingsService.updateMeeting(id, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound)
                call.respond(updatedMeeting)
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val success = meetingsService.deleteMeeting(id)
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post("/{id}/register") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val userId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val result = meetingsService.registerUserForMeeting(userId, id)
                call.respond(result)
            }

            post("/{id}/unregister") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val userId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val result = meetingsService.unregisterUserFromMeeting(userId, id)
                call.respond(result)
            }

            post("/{id}/end") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val success = meetingsService.markMeetingAsEnded(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Meeting marked as ended"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Failed to mark meeting as ended"))
                }
            }

            get("/user/planned") {
                val userId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val meetings = meetingsService.getUserPlannedMeetings(userId)
                call.respond(meetings)
            }

            get("/user/passed") {
                val userId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val meetings = meetingsService.getUserPassedMeetings(userId)
                call.respond(meetings)
            }

            get("/{id}/is-registered") {
                val meetingId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val userId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val isRegistered = meetingsService.isUserRegisteredForMeeting(userId, meetingId)
                call.respond(mapOf("isRegistered" to isRegistered))
            }
        }
    }
}