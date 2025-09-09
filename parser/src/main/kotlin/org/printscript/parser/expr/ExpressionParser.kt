package org.printscript.parser.expr

import org.printscript.ast.Expression
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.token.TokenStream

interface ExpressionParser {
    fun parseExpression(tokenStream: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError>
}
