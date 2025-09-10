package org.printscript.lexer

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.error.LexerError
import org.printscript.token.EofToken
import org.printscript.token.Token

object TokenCollector {

    fun collectUntil(
        initial: Tokenizer,
        stopWhen: (Token) -> Boolean,
    ): Result<List<Token>, LexerError> {
        val tokens = mutableListOf<Token>()
        var currentTokenizer = initial

        while (true) {
            when (val step = currentTokenizer.next()) {
                is Failure -> return step
                is Success -> {
                    val (tok, nextTok) = step.value
                    tokens += tok
                    if (stopWhen(tok)) return Success(tokens)
                    currentTokenizer = nextTok
                }
            }
        }
    }

    fun collectAll(initial: Tokenizer): Result<List<Token>, LexerError> =
        collectUntil(initial) { it is EofToken }
}
