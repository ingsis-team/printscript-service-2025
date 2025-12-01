package com.ingsis_team.printscript_service_2025.redis.route

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.ingsis_team.printscript_service_2025.model.dto.FormatterRulesFileDTO
import com.ingsis_team.printscript_service_2025.model.dto.LinterRulesFileDTO
import com.ingsis_team.printscript_service_2025.redis.dto.ChangeRulesDTO
import com.ingsis_team.printscript_service_2025.redis.dto.Snippet
import com.ingsis_team.printscript_service_2025.redis.producer.SnippetFormatterProducer
import com.ingsis_team.printscript_service_2025.redis.producer.SnippetLintProducer
import com.ingsis_team.printscript_service_2025.redis.producer.SnippetTestProducer
import com.ingsis_team.printscript_service_2025.service.FormatterRulesService
import com.ingsis_team.printscript_service_2025.service.LinterRulesService

@RestController
@RequestMapping("redis")
class RedisController
    @Autowired
    constructor(
        private val formatProducer: SnippetFormatterProducer,
        private val lintProducer: SnippetLintProducer,
        private val testProducer: SnippetTestProducer,
        private val formatterService: FormatterRulesService,
        private val linterRulesService: LinterRulesService,
    ) {
        private val logger = LoggerFactory.getLogger(RedisController::class.java)

        @PutMapping("/format")
        suspend fun changeAndFormatRules(
            @RequestBody data: ChangeRulesDTO,
        ) {
            logger.info("Received change and format rules request for userId: ${data.userId}")
            logger.debug("Rules details: ${data.rules}")
            logger.info("Received data: $data")

            val spaceBeforeColon = data.rules.find { it.name == "spaceBeforeColon" }?.isActive ?: false
            val spaceAfterColon = data.rules.find { it.name == "spaceAfterColon" }?.isActive ?: false
            val spaceAroundEquals = data.rules.find { it.name == "spaceAroundEquals" }?.isActive ?: false
            val lineBreak = data.rules.find { it.name == "lineBreak" }?.value as? Int ?: 0
            val lineBreakPrintln = data.rules.find { it.name == "lineBreakPrintln" }?.value as? Int ?: 0
            val conditionalIndentation = data.rules.find { it.name == "conditionalIndentation" }?.value as? Int ?: 0

            val formatterDto =
                FormatterRulesFileDTO(
                    spaceBeforeColon,
                    spaceAfterColon,
                    spaceAroundEquals,
                    lineBreak,
                    lineBreakPrintln,
                    conditionalIndentation,
                )
            logger.info(
                "formatterDto: userId=${data.userId}, spaceBeforeColon=${formatterDto.spaceBeforeColon}," +
                    " spaceAfterColon=${formatterDto.spaceAfterColon}, " +
                    "spaceAroundEquals=${formatterDto.spaceAroundEquals}, lineBreak=${formatterDto.lineBreak}," +
                    " lineBreakPrintln=${formatterDto.lineBreakPrintln}," +
                    " conditionalIndentation=${formatterDto.conditionalIndentation}",
            )

            formatterService.updateFormatterRules(formatterDto, data.userId)
            logger.info("Rules updated")
            data.snippets.forEach {
                val snippet = Snippet(data.userId, it.snippetId, it.input, data.correlationId)
                formatProducer.publishEvent(snippet)
            }
            logger.info("Rules published")
        }

        @PutMapping("lint")
        suspend fun changeAndLintRules(
            @RequestBody data: ChangeRulesDTO,
        ) {
            logger.info("Received change and lint rules request for userId: ${data.userId}")
            logger.debug("Rules details: ${data.rules}")
            logger.info("Received data2: $data")

            val identifierFormat = data.rules.find { it.name == "identifierFormat" }?.value as? String ?: ""
            val enablePrintOnly = data.rules.find { it.name == "enablePrintOnly" }?.isActive ?: false
            val enableInputOnly = data.rules.find { it.name == "enableInputOnly" }?.isActive ?: false

            val linterDto =
                LinterRulesFileDTO(
                    data.userId,
                    identifierFormat,
                    enablePrintOnly,
                    enableInputOnly,
                )

            logger.info(
                "linterDto: userId=${linterDto.userId}, identifierFormat=${linterDto.identifier_format}," +
                    " enablePrintOnly=${linterDto.enablePrintOnly}, " +
                    "enableInputOnly=${linterDto.enableInputOnly}",
            )

            linterRulesService.updateLinterRules(linterDto, data.userId)

            logger.info("Rules updated1")
            data.snippets.map {
                val snippet = Snippet(data.userId, it.snippetId, it.input, data.correlationId)
                lintProducer.publishEvent(snippet)
            }
            logger.info("Rules published2")
        }

        @PutMapping("/publish/format/snippet")
        suspend fun formatSnippet(
            @RequestBody snippet: Snippet,
        ) {
            logger.info("Formatting snippet: ${snippet.id} for user: ${snippet.userId}")
            formatProducer.publishEvent(snippet)
            logger.info("Format event published for snippet: ${snippet.id}")
        }

        @PutMapping("/publish/lint/snippet")
        suspend fun lintSnippet(
            @RequestBody snippet: Snippet,
        ) {
            logger.info("Linting snippet: ${snippet.id} for user: ${snippet.userId}")
            lintProducer.publishEvent(snippet)
            logger.info("Lint event published for snippet: ${snippet.id}")
        }

        @PutMapping("/publish/test/snippet")
        suspend fun testSnippet(
            @RequestBody snippet: Snippet,
        ) {
            logger.info("Testing snippet: ${snippet.id} for user: ${snippet.userId}")
            testProducer.publishEvent(snippet)
            logger.info("Test event published for snippet: ${snippet.id}")
        }
    }
