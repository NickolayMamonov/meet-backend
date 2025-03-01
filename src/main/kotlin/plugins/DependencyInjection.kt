package dev.whysoezzy.plugins

import dev.whysoezzy.data.repositories.MeetingsRepository
import dev.whysoezzy.services.MeetingsService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

val appModule = module {
    // Config
    single { JWTConfig(environment.config) }

    // Utils
    single { SMSService(environment.config) }

    // Repositories
    single { MeetingsRepository() }
    single { CommunitiesRepository() }
    single { UsersRepository() }

    // Services
    single { MeetingsService(get(), get()) }
    single { CommunitiesService(get(), get()) }
    single { UsersService(get()) }
    single { AuthService(get(), get(), get()) }
}