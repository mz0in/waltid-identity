package id.walt.verifier

import id.walt.verifier.base.config.ConfigManager
import id.walt.verifier.base.config.WebConfig
import id.walt.ssikit.did.DidService
import id.walt.ssikit.did.resolver.LocalResolver
import id.walt.verifier.OidcApi.oidcApi
import id.walt.verifier.base.web.plugins.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

private val log = KotlinLogging.logger { }

suspend fun main(args: Array<String>) {
    log.debug { "verfier CLI starting ..." }

    log.debug { "Init walt services..." }
    //WaltidServices.init()
    DidService.apply {
        registerResolver(LocalResolver())
        updateResolversForMethods()
    }
    //ServiceMatrix("service-matrix.properties")

    log.info { "Reading configurations..." }
    ConfigManager.loadConfigs(args)

    val webConfig = ConfigManager.getConfig<WebConfig>()

    log.info { "Starting web server (binding to ${webConfig.webHost}, listening on port ${webConfig.webPort})..." }
    embeddedServer(CIO, port = webConfig.webPort, host = webConfig.webHost, module = Application::module)
        .start(wait = true)
}

fun Application.configurePlugins() {
    configureHTTP()
    configureMonitoring()
    configureStatusPages()
    configureSerialization()
    configureRouting()
    configureOpenApi()
}

fun Application.module() {
    configurePlugins()
    oidcApi()
    verfierApi()
}