package dev.whysoezzy.routes

import dev.whysoezzy.data.model.CommunityRequest
import dev.whysoezzy.services.CommunitiesService
import dev.whysoezzy.utils.userId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.communitiesRoutes(communitiesService: CommunitiesService) {

    route("/communities") {
        // Public routes
        get {
            call.respond(communitiesService.getAllCommunities())
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val community = communitiesService.getCommunityById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(community)
        }

        get("/title/{title}") {
            val title = call.parameters["title"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val community = communitiesService.getCommunityByTitle(title)
                ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(community)
        }

        get("/{id}/meetings") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val meetings = communitiesService.getCommunityMeetings(id)
            call.respond(meetings)
        }

        // Protected routes
        authenticate {
            post {
                val request = call.receive<CommunityRequest>()
                val community = communitiesService.createCommunity(request)
                call.respond(HttpStatusCode.Created, community)
            }

            put("/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<CommunityRequest>()
                val updatedCommunity = communitiesService.updateCommunity(id, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound)
                call.respond(updatedCommunity)
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val success = communitiesService.deleteCommunity(id)
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post("/{id}/join") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val userId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val result = communitiesService.addMemberToCommunity(id, userId)
                call.respond(result)
            }

            post("/{id}/leave") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val userId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val result = communitiesService.removeMemberFromCommunity(id, userId)
                call.respond(result)
            }

            post("/{communityId}/meetings/{meetingId}") {
                val communityId = call.parameters["communityId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest)
                val meetingId = call.parameters["meetingId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                val success = communitiesService.addMeetingToCommunity(communityId, meetingId)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Meeting added to community"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Failed to add meeting to community"))
                }
            }

            delete("/{communityId}/meetings/{meetingId}") {
                val communityId = call.parameters["communityId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val meetingId = call.parameters["meetingId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val success = communitiesService.removeMeetingFromCommunity(communityId, meetingId)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Meeting removed from community"))
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("message" to "Failed to remove meeting from community")
                    )
                }
            }

            get("/user") {
                val userId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val communities = communitiesService.getUserCommunities(userId)
                call.respond(communities)
            }

            get("/{id}/is-member") {
                val communityId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val userId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val isMember = communitiesService.isUserMemberOfCommunity(communityId, userId)
                call.respond(mapOf("isMember" to isMember))
            }
        }
    }
}
