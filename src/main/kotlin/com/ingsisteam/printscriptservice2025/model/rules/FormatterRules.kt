package com.ingsisteam.printscriptservice2025.model.rules
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.*

@Entity
class FormatterRules(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),
    @Column(name = "userId", unique = true)
    val userId: String? = null,
    @Column(name = "spaceBeforeColon")
    var spaceBeforeColon: Boolean = true,
    @Column(name = "spaceAfterColon")
    var spaceAfterColon: Boolean = true,
    @Column(name = "spaceAroundEquals")
    var spaceAroundEquals: Boolean = true,
    @Column(name = "lineBreak")
    var lineBreak: Int = 1,
    @Column(name = "lineBreakPrintln")
    var lineBreakPrintln: Int = 1,
    @Column(name = "conditionalIndentation")
    var conditionalIndentation: Int = 4,
) {
    override fun toString(): String {
        return "Los valores son userId: $userId, spaceBC: $spaceBeforeColon, spaceAC:$spaceAfterColon, spaceAE:$spaceAroundEquals,"
    }
}
