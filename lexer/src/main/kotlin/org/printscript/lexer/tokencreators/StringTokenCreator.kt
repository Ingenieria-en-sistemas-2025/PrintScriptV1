package org.printscript.lexer.tokencreators

import org.printscript.lexer.Lexeme
import org.printscript.lexer.error.UnterminatedString
import org.printscript.token.StringLiteralToken
import org.printscript.token.Token

object StringTokenCreator : TokenCreator {
    override fun create(lexeme: Lexeme): Token {
        val raw = lexeme.text
        val open = raw.firstOrNull()
        val close = raw.lastOrNull()
        val isQuote = { c: Char? -> c == '"' || c == '\'' }

        // si no cierra con la misma comilla → error léxico
        if (open == null || close == null) {
            throw UnterminatedString(lexeme.span)
        }
        if (open != close) {
            throw UnterminatedString(lexeme.span)
        }
        if (!isQuote(open)) {
            throw UnterminatedString(lexeme.span)
        }

        val inner = unquote(raw)
        val value = unescapeBasic(inner)
        return StringLiteralToken(value, lexeme.span)
    }

    private fun unquote(raw: String): String {
        return raw.substring(1, raw.length - 1)
    }

    private fun unescapeBasic(inner: String): String {
        val out = StringBuilder(inner.length)
        var i = 0
        while (i < inner.length) {
            val c = inner[i]
            if (c != '\\') {
                out.append(c)
                i++
                continue
            }

            if (i + 1 >= inner.length) {
                out.append('\\')
                i++
                continue
            }

            val n = inner[i + 1]
            val mapped = mapEscape(n)
            out.append(mapped ?: n) // desconocido: descarto '\' y dejo n
            i += 2
        }
        return out.toString()
    }

    private fun mapEscape(n: Char): Char? = when (n) {
        '\\' -> '\\'
        '"' -> '"'
        '\'' -> '\''
        'n' -> '\n'
        't' -> '\t'
        'r' -> '\r'
        else -> null
    }
}
