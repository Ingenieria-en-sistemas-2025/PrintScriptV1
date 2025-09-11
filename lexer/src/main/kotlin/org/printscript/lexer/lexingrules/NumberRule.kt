package org.printscript.lexer.lexingrules

private val NUM_REGEX = Regex("\\d+(?:\\.\\d+)?")

class NumberRule(override val key: RuleKey) : LexingRule {
    override fun matchLength(input: CharSequence): Int =
        NUM_REGEX.matchAt(input, 0)?.value?.length ?: 0
}
