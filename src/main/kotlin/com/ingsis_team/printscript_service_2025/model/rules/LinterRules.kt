package com.ingsis_team.printscript_service_2025.model.rules

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.*

@Entity
class LinterRules(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    var id: UUID? = UUID.randomUUID(),
    @Column(name = "userId")
    var userId: String? = null,
    @Column(name = "identifier_format")
    var identifierFormat: String = "camelcase",
    @Column(name = "enablePrintOnly")
    var enablePrintOnly: Boolean = true,
    @Column(name = "enableInputOnly")
    var enableInputOnly: Boolean = true,
)
