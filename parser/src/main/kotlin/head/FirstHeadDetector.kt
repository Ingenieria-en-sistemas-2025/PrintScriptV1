package head

import IdentifierToken
import KeywordToken
import OperatorToken
import TokenStream

class FirstHeadDetector : HeadDetector {
    override fun detect(tokenStream: TokenStream): Head {
        val token0 = tokenStream.peek()
        return when (token0) {
            is KeywordToken -> Kw(token0.kind)
            is IdentifierToken -> {
                val token1 = tokenStream.peek(lookahead = 1)
                if (token1 is OperatorToken && token1.operator == Operator.ASSIGN) Assign else Unknown
            }
            else -> Unknown
        }
    }
}
