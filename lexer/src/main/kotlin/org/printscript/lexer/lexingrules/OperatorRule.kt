package org.printscript.lexer.lexingrules

import org.printscript.common.Operator
import org.printscript.common.Span
import org.printscript.token.OperatorToken
import org.printscript.token.Token

class OperatorRule(map: Map<String, Operator>) : LexingRule {

    private val operators = map.toMap()
    private val keys = operators.keys

    override fun matchLength(input: CharSequence): Int {
        var best = 0
        for (k in keys) {
            if (startsWith(input, k) && k.length > best) best = k.length
        }
        return best
    }

    override fun build(lexeme: String, span: Span): Token =
        OperatorToken(requireNotNull(operators[lexeme]) { "Operador desconocido: $lexeme" }, span)

    private fun startsWith(cs: CharSequence, s: String): Boolean {
        if (s.length > cs.length) return false
        for (i in s.indices) if (cs[i] != s[i]) return false
        return true
    }
}
