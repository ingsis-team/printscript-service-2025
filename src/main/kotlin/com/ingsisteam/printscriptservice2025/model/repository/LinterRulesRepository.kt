package com.ingsisteam.printscriptservice2025.model.repository

import com.ingsisteam.printscriptservice2025.model.rules.LinterRules
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LinterRulesRepository : JpaRepository<LinterRules, UUID> {
    fun findByUserId(userId: String): Optional<LinterRules>
}
