package org.printscript.parser

import org.printscript.ast.ProgramNode
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.token.TokenStream

interface Parser {
    fun parse(tokenStream: TokenStream): Result<ProgramNode, LabeledError>
}
