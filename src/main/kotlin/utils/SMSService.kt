package dev.whysoezzy.utils

import io.ktor.server.config.*
import org.slf4j.LoggerFactory

class SMSService(private val config: ApplicationConfig) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val enabled = config.propertyOrNull("sms.enabled")?.getString()?.toBoolean() ?: false
    private val apiKey = config.propertyOrNull("sms.apiKey")?.getString() ?: ""

    /**
     * In a real application, this would connect to an SMS service provider.
     * For development purposes, we just log the OTP.
     */
    fun sendOtp(phoneNumber: String, otp: String): Boolean {
        logger.info("Sending OTP ($otp) to phone: $phoneNumber")

        if (!enabled) {
            logger.info("SMS service is disabled. Would have sent OTP: $otp to $phoneNumber")
            return true
        }

        try {
            // Here you would implement the actual SMS sending logic
            // using a service like Twilio, MessageBird, etc.
            // For example with Twilio:
            //
            // val twilioAccountSid = "YOUR_ACCOUNT_SID"
            // val twilioAuthToken = "YOUR_AUTH_TOKEN"
            // val twilioClient = Twilio.init(twilioAccountSid, twilioAuthToken)
            // val message = Message.creator(
            //     PhoneNumber(phoneNumber),
            //     PhoneNumber("YOUR_TWILIO_PHONE_NUMBER"),
            //     "Your verification code is: $otp"
            // ).create()

            logger.info("Successfully sent OTP to $phoneNumber")
            return true
        } catch (e: Exception) {
            logger.error("Failed to send OTP to $phoneNumber", e)
            return false
        }
    }
}