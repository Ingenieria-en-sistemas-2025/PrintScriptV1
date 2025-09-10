package org.printscript.parser.stmt

import org.printscript.ast.ConstDeclaration
import org.printscript.ast.Statement
import org.printscript.common.Keyword
import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Result
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.parser.ParserUtils.expectIdentifier
import org.printscript.parser.ParserUtils.expectKeyword
import org.printscript.parser.ParserUtils.expectOperator
import org.printscript.parser.ParserUtils.expectSeparator
import org.printscript.parser.ParserUtils.expectTypeToken
import org.printscript.parser.expr.ExpressionParser
import org.printscript.token.TokenStream

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
