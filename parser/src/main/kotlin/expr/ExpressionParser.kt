package expr

import Expression
import TokenStream

interface ExpressionParser {
    fun parseExpression(tokenStream: TokenStream): Expression
}