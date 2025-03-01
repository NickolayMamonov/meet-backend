package dev.whysoezzy

import dev.whysoezzy.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    DatabaseConfig.init(environment.config)

    // Setup plugins
    configureSerialization()
    configureSecurity()
    configureMonitoring()
    configureDependencyInjection()
    configureCORS()
    configureStatusPages()
    configureRouting()
}