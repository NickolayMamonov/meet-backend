package dev.whysoezzy.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val errorResponse = ErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                message = cause.message ?: "An unknown error occurred"
            )
            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }

        status(HttpStatusCode.NotFound) { call, status ->
            val errorResponse = ErrorResponse(
                status = status.value,
                message = "The requested resource was not found"
            )
            call.respond(status, errorResponse)
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            val errorResponse = ErrorResponse(
                status = status.value,
                message = "Authentication required"
            )
            call.respond(status, errorResponse)
        }
    }
}

@Serializable
data class ErrorResponse(
    val status: Int,
    val message: String
)