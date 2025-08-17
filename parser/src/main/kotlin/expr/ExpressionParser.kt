package expr

import TokenStream

interface ExpressionParser {
    fun parseExpression(tokenStream: TokenStream): Expression
}