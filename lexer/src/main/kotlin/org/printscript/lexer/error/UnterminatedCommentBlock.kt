package org.printscript.lexer.error

import org.printscript.common.Span

data class UnterminatedCommentBlock(override val span: Span) : LexerError {
    override val message = "Comentario de bloque no cerrado"
}
