package org.printscript.parser.expr
import org.printscript.ast.Expression
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.token.TokenStream

interface PrefixParselet {
    fun parse(p: ExpressionParser, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError>
}
