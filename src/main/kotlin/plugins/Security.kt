package dev.whysoezzy.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val jwtConfig by inject<JWTConfig>()

    authentication {
        jwtConfig.configureAuth(environment.config, this)
    }
}