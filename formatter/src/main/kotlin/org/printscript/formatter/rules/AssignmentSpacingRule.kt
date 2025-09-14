package org.printscript.formatter.rules

import org.printscript.common.Operator
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.OperatorToken
import org.printscript.token.Token

class AssignmentSpacingRule(private val cfg: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        val around = cfg.spaceAroundAssignment
        if (current is OperatorToken && current.operator == Operator.ASSIGN) {
            return if (around) " " else null
        }
        if (prev is OperatorToken && prev.operator == Operator.ASSIGN) {
            return if (around) " " else null
        }
        return null
    }
}
