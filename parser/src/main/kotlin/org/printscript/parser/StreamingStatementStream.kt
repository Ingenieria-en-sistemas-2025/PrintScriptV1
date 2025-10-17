package org.printscript.parser

import org.printscript.ast.Statement
import org.printscript.ast.StatementStream
import org.printscript.ast.Step
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.parser.head.HeadDetector
import org.printscript.token.EofToken
import org.printscript.token.TokenStream

private const val MAX_RECOVERY_STEPS = 10_000
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

    override fun nextStep(): Step {
        if (ts.isEof()) return Step.Eof
        return when (val result = parseOne(ts)) {
            is Success -> {
                val (stmt, rest) = result.value
                Step.Item(stmt, of(rest, parseOne, headDetector))
            }
            is Failure -> {
                val sync = Recovery.syncToNextHeadTopLevel(ts, headDetector)
                val progressed = if (sync.next === ts) {
                    val afterOne = Recovery.advanceOne(ts) ?: ts
                    advanceToNextHeadOrEof(afterOne, headDetector)
                } else {
                    sync.next
                }
                Step.Error(result.error, of(progressed, parseOne, headDetector))
            }
        }
    }

    private fun advanceToNextHeadOrEof(start: TokenStream, hd: HeadDetector): TokenStream {
        var cur = start
        var steps = 0
        while (true) {
            if (steps++ > MAX_RECOVERY_STEPS) return cur
            val pk = cur.peek()
            if (pk is Failure) return cur
            val tok = (pk as Success).value
            if (tok is EofToken) return cur
            if (isTopLevelHead(cur, hd)) return cur
            cur = Recovery.advanceOne(cur) ?: return cur
        }
    }

    private fun isTopLevelHead(ts: TokenStream, hd: HeadDetector): Boolean =
        when (val r = hd.detect(ts)) {
            is Success -> r.value !is org.printscript.parser.head.Unknown
            is Failure -> false
        }
}
