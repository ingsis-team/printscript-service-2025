package com.ingsisteam.printscriptservice2025.model.repository

import com.ingsisteam.printscriptservice2025.model.rules.FormatterRules
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FormatterRulesRepository : JpaRepository<FormatterRules, UUID> {
    fun findByUserId(userId: String): Optional<FormatterRules>
}
