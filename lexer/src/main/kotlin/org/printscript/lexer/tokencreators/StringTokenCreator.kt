package org.printscript.lexer.tokencreators

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.lexer.Lexeme
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.error.UnterminatedString
import org.printscript.token.StringLiteralToken
import org.printscript.token.Token

object StringTokenCreator : TokenCreator {

    override fun create(lexeme: Lexeme): Result<Token, LexerError> {
        val raw = lexeme.text

        return when (val v = validateBalancedQuotes(raw, lexeme.span)) {
            is Failure -> Failure(v.error)
            is Success -> {
                val inner = unquote(raw)
                val value = unescapeBasic(inner)
                Success(StringLiteralToken(value, lexeme.span))
            }
        }
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

    private fun validateBalancedQuotes(raw: String, span: Span): Result<Char, LexerError> {
        val open = raw.firstOrNull()
        val close = raw.lastOrNull()
        val isQuote = { c: Char? -> c == '"' || c == '\'' }

        if (open == null || close == null) {
            return Failure(UnterminatedString(span))
        }
        if (open != close) {
            return Failure(UnterminatedString(span))
        }
        if (!isQuote(open)) {
            return Failure(UnterminatedString(span))
        }
        return Success(open)
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
