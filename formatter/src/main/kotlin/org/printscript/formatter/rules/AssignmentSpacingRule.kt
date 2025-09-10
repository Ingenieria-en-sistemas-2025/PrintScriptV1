package org.printscript.formatter.rules

import org.printscript.common.Operator
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.OperatorToken
import org.printscript.token.Token

class AssignmentSpacingRule(private val cfg: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        val around = cfg.spaceAroundAssignment
        // ANTES del = (prefijo del =)
        if (current is OperatorToken && current.operator == Operator.ASSIGN) {
            return if (around) " " else ""
        }
        // DESPUES del = (prefijo del token de la derecha)
        if (prev is OperatorToken && prev.operator == Operator.ASSIGN) {
            return if (around) " " else ""
        }
        return null
    }
}
