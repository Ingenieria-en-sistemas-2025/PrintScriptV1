package org.printscript.lexer.lexingrules

object BooleanLiteralRule : LexingRule {
    override val key: RuleKey = RuleKey("BOOLEAN_LITERAL")
    private val re = Regex("""(?:true|false)\b""")
    override fun matchLength(input: CharSequence): Int =
        re.matchAt(input, 0)?.value?.length ?: 0
}
