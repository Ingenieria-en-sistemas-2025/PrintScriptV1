package org.printscript.token.dsl

import org.printscript.common.Separator

fun TokenBuilder.sep() = SeparatorDsl(this)
class SeparatorDsl(private val b: TokenBuilder) {
    fun lparen() = b.separator(Separator.LPAREN)
    fun rparen() = b.separator(Separator.RPAREN)
    fun lbrace() = b.separator(Separator.LBRACE)
    fun rbrace() = b.separator(Separator.RBRACE)
    fun colon() = b.separator(Separator.COLON)
    fun semicolon() = b.separator(Separator.SEMICOLON)
}
