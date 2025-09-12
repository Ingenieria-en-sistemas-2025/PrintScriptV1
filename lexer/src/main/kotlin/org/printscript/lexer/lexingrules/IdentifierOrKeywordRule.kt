package org.printscript.lexer.lexingrules

private val IDENT_REGEX = Regex("[A-Za-z_][A-Za-z0-9_]*")

class IdentifierOrKeywordRule(override val key: RuleKey) : LexingRule {

    override fun matchLength(input: CharSequence): Int =
        IDENT_REGEX.matchAt(input, 0)?.value?.length ?: 0
}
