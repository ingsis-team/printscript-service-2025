package com.ingsis_team.printscript_service_2025.model.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.ingsis_team.printscript_service_2025.model.rules.FormatterRules
import java.util.*

interface FormatterRulesRepository : JpaRepository<FormatterRules, UUID> {
    fun findByUserId(userId: String): Optional<FormatterRules>
}
