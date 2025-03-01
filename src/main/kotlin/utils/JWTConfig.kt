package dev.whysoezzy.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import java.util.*

class JWTConfig(private val config: ApplicationConfig) {
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    private val validityInMs = config.property("jwt.validityMs").getString().toLong()

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(userId: String): String = JWT.create()
        .withSubject(userId)
        .withIssuer(issuer)
        .withAudience(audience)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(algorithm)

    fun configureAuth(config: ApplicationConfig, authConfig: AuthenticationConfig) {
        authConfig.jwt {
            realm = config.property("jwt.realm").getString()
            verifier(
                JWT.require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(audience) &&
                    credential.payload.expiresAt.after(Date())) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}

// Extension to easily get user ID from ApplicationCall
val ApplicationCall.userId: String?
    get() = authentication.principal<JWTPrincipal>()?.subject