package org.printscript.lexer.lexingrules

import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class SeparatorRule(map: Map<String, Separator>) : LexingRule {
    private val seps = map.toMap()
    private val keys = seps.keys

    override fun matchLength(input: CharSequence): Int {
        var best = 0
        for (k in keys) {
            if (startsWith(input, k) && k.length > best) best = k.length
        }
        return best
    }

    override fun build(lexeme: String, span: Span): Token =
        SeparatorToken(requireNotNull(seps[lexeme]) { "Separador desconocido: $lexeme" }, span)

    private fun startsWith(cs: CharSequence, s: String): Boolean {
        if (s.length > cs.length) return false
        for (i in s.indices) if (cs[i] != s[i]) return false
        return true
    }
}
