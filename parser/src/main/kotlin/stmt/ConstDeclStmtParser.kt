package stmt

import ConstDeclaration
import Keyword
import LabeledError
import Operator
import ParserUtils.expectIdentifier
import ParserUtils.expectKeyword
import ParserUtils.expectOperator
import ParserUtils.expectSeparator
import ParserUtils.expectTypeToken
import Result
import Separator
import Span
import Statement
import TokenStream
import expr.ExpressionParser

object ConstDeclStmtParser : StmtParser {
    override fun parse(ts: TokenStream, expressionParser: ExpressionParser): Result<Pair<Statement, TokenStream>, LabeledError> =
        expectKeyword(ts, Keyword.CONST).flatMap { (kw, t1) ->
            expectIdentifier(t1).flatMap { (name, t2) ->
                expectSeparator(t2, Separator.COLON).flatMap { (_, t3) ->
                    expectTypeToken(t3).flatMap { (typeTok, t4) ->
                        expectOperator(t4, Operator.ASSIGN).flatMap { (_, t5) ->
                            expressionParser.parseExpression(t5).flatMap { (init, t6) ->
                                expectSeparator(t6, Separator.SEMICOLON).map { (semi, t7) ->
                                    val span = Span(kw.span.start, semi.span.end)
                                    ConstDeclaration(name.identifier, typeTok.type, init, span) to t7
                                }
                            }
                        }
                    }
                }
            }
        }
}
