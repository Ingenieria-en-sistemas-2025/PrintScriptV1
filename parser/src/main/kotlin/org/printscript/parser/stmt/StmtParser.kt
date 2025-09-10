package org.printscript.parser.stmt

import org.printscript.ast.Statement
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.parser.expr.ExpressionParser
import org.printscript.token.TokenStream

interface StmtParser {
    fun parse(ts: TokenStream, expressionParser: ExpressionParser): Result<Pair<Statement, TokenStream>, LabeledError>
}
