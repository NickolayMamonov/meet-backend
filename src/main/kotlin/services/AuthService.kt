package dev.whysoezzy.services

import dev.whysoezzy.data.model.AuthResponse
import dev.whysoezzy.data.model.PhoneAuthRequest
import dev.whysoezzy.data.model.UserRequest
import dev.whysoezzy.data.repositories.UsersRepository
import dev.whysoezzy.utils.JWTConfig
import dev.whysoezzy.utils.SMSService
import java.util.concurrent.ConcurrentHashMap

class AuthService(
    private val usersRepository: UsersRepository,
    private val smsService: SMSService,
    private val jwtConfig: JWTConfig
) {
    // In a production app, this would be stored in Redis or similar
    private val otpStorage = ConcurrentHashMap<String, String>()

    fun requestPhoneAuth(request: PhoneAuthRequest): Boolean {
        val otp = generateOtp()
        otpStorage[request.phoneNumber] = otp

        return smsService.sendOtp(request.phoneNumber, otp)
    }

    fun verifyOtp(phoneNumber: String, otpCode: String): AuthResponse? {
        val storedOtp = otpStorage[phoneNumber]

        if (storedOtp == null || storedOtp != otpCode) {
            return null
        }

        // Clean up OTP after successful verification
        otpStorage.remove(phoneNumber)

        // Check if user exists or create a new one
        var user = usersRepository.getUserByPhone(phoneNumber)

        if (user == null) {
            // Create a new user if not exists with default name
            val newUser = UserRequest(name = "User")
            user = usersRepository.createUser(phoneNumber, newUser)
        }

        // Generate JWT token
        val token = jwtConfig.generateToken(user.id)

        // Get full user profile for response
        val userProfile = usersRepository.getUserProfileById(user.id)!!

        return AuthResponse(token, user.id, userProfile)
    }

    private fun generateOtp(): String {
        // In a production app, use a more secure method
        return (1000..9999).random().toString()
    }
}