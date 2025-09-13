package org.printscript.parser

import org.printscript.ast.Statement
import org.printscript.ast.StatementStream
import org.printscript.ast.Step
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.parser.head.HeadDetector
import org.printscript.token.TokenStream

class StreamingStatementStream private constructor(
    private val ts: TokenStream,
    private val parseOne: (TokenStream) -> Result<Pair<Statement, TokenStream>, LabeledError>,
    private val headDetector: HeadDetector,
) : StatementStream {

    companion object {
        fun of(
            ts: TokenStream,
            parseOne: (TokenStream) -> Result<Pair<Statement, TokenStream>, LabeledError>,
            headDetector: HeadDetector,
        ): StatementStream = StreamingStatementStream(ts, parseOne, headDetector)
    }

    override fun step(): Step {
        if (ts.isEof()) return Step.Eof
        return when (val result = parseOne(ts)) {
            is Success -> {
                val (stmt, rest) = result.value
                Step.Item(stmt, of(rest, parseOne, headDetector))
            }
            is Failure -> {
                val syncResult = Recovery.syncToNextHeadTopLevel(ts, headDetector) // tokenStream resync con Head topLevel
                Step.Error(result.error, of(syncResult.next, parseOne, headDetector))
            }
        }
    }
}
