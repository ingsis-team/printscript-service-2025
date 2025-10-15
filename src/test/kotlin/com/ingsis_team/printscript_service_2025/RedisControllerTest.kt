package com.ingsis_team.printscript_service_2025
import org.mockito.Mockito.mock
import com.ingsis_team.printscript_service_2025.redis.producer.SnippetFormatterProducer
import com.ingsis_team.printscript_service_2025.redis.producer.SnippetLintProducer
import com.ingsis_team.printscript_service_2025.redis.route.RedisController
import com.ingsis_team.printscript_service_2025.service.FormatterRulesService
import com.ingsis_team.printscript_service_2025.service.LinterRulesService

class RedisControllerTest {

    private val formatProducer = mock(SnippetFormatterProducer::class.java)
    private val lintProducer = mock(SnippetLintProducer::class.java)
    private val formatterService = mock(FormatterRulesService::class.java)
    private val linterRulesService = mock(LinterRulesService::class.java)

    private val controller = RedisController(
        formatProducer = formatProducer,
        lintProducer = lintProducer,
        formatterService = formatterService,
        linterRulesService = linterRulesService
    )

}