package org.printscript.lexer.lexingrules

class IdentifierOrKeywordRule(override val key: RuleKey, val pattern: Regex) : LexingRule {
    override fun matchLength(input: CharSequence): Int =
        pattern.matchAt(input, 0)?.value?.length ?: 0
}
