package com.ingsisteam.printscriptservice2025.config

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class StartupLoggerTest {

    @Test
    fun `onApplicationReady should log startup messages`() {
        val startupLogger = StartupLogger()

        // Attach a ListAppender to capture logs
        val logger = LoggerFactory.getLogger(StartupLogger::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)

        // Trigger event
        startupLogger.onApplicationReady()

        // Verify at least one of the known messages is present
        val messages = listAppender.list.map { it.formattedMessage }
        assert(messages.any { it.contains("PRINTSCRIPT SERVICE IS RUNNING!") })
        assert(messages.any { it.contains("API Docs:") })

        // Cleanup
        logger.detachAppender(listAppender)
    }
}
