package com.ingsisteam.printscriptservice2025.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.jvm.java

@Component
class StartupLogger {
    private val logger = LoggerFactory.getLogger(StartupLogger::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        logger.info("\n============================================================")
        logger.info("PRINTSCRIPT SERVICE IS RUNNING!")
        logger.info("Server: http://localhost:8082")
        logger.info("API Docs: http://localhost:8082/swagger-ui.html")
        logger.info("============================================================\n")
    }
}
