package org.printscript.lexer.lexingrules

interface LexingRule {
    val key: RuleKey
    fun matchLength(input: CharSequence): Int
}
