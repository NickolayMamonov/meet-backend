package dev.whysoezzy.routes

import dev.whysoezzy.data.model.OtpVerificationRequest
import dev.whysoezzy.data.model.PhoneAuthRequest
import dev.whysoezzy.services.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/request-otp") {
            val request = call.receive<PhoneAuthRequest>()

            val success = authService.requestPhoneAuth(request)
            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "OTP sent successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Failed to send OTP"))
            }
        }

        post("/verify-otp") {
            val request = call.receive<OtpVerificationRequest>()

            val authResponse = authService.verifyOtp(request.phoneNumber, request.otpCode)
            if (authResponse != null) {
                call.respond(authResponse)
            } else {
                call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid OTP code"))
            }
        }
    }
}
