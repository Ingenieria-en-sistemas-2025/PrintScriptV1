package org.printscript.parser

import org.printscript.ast.StatementStream
import org.printscript.token.TokenStream

interface Parser {
    fun parse(tokenStream: TokenStream): StatementStream
}
