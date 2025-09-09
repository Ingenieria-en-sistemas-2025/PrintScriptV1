package org.printscript.parser.expr

import org.printscript.ast.Expression
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.token.TokenStream

interface InfixParselet {
    val prec: Prec
    fun parse(p: ExprPratt, left: Expression, ts: TokenStream): Result<Pair<Expression, TokenStream>, LabeledError>
}
