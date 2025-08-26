package head

import IdentifierToken
import KeywordToken
import LabeledError
import OperatorToken
import Result
import TokenStream

class FirstHeadDetector : HeadDetector {
    override fun detect(tokenStream: TokenStream): Result<Head, LabeledError> =
        tokenStream.peek().map { t0 ->
            when (t0) {
                is KeywordToken -> Kw(t0.kind)
                is IdentifierToken -> {
                    val t1 = tokenStream.peek(1).getOrNull()
                    if (t1 is OperatorToken && t1.operator == Operator.ASSIGN) Assign else Unknown
                }
                else -> Unknown
            }
        }
}
