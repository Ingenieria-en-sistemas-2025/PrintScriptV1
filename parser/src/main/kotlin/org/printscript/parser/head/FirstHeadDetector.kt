package org.printscript.parser.head

import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Result
import org.printscript.token.IdentifierToken
import org.printscript.token.KeywordToken
import org.printscript.token.OperatorToken
import org.printscript.token.TokenStream

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
