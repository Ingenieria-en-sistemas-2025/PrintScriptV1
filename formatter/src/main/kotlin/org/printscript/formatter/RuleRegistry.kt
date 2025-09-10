package org.printscript.formatter

import org.printscript.token.Token

interface RuleRegistry {
    fun findApplicableRule(prev: Token?, current: Token, next: Token?): String?
}
