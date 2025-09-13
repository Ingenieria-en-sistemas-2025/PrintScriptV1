package org.printscript.parser

import org.printscript.common.Failure
import org.printscript.common.Separator
import org.printscript.common.Success
import org.printscript.parser.head.HeadDetector
import org.printscript.parser.head.Unknown
import org.printscript.token.EofToken
import org.printscript.token.SeparatorToken
import org.printscript.token.TokenStream

object Recovery {
    data class SyncResult(val next: TokenStream)

    fun syncToNextHeadTopLevel(
        ts0: TokenStream,
        headDetector: HeadDetector,
    ): SyncResult = syncRec(ts0, 0, headDetector)

    private fun syncRec(
        ts: TokenStream,
        depth: Int,
        headDetector: HeadDetector,
    ): SyncResult {
        val peek = ts.peek()
        if (peek is Failure) return SyncResult(ts)
        val tok = (peek as Success).value
        if (tok is EofToken) return SyncResult(ts)

        if (tok is SeparatorToken) {
            val step = handleSeparator(tok, ts, depth)
            return if (step.stop) {
                SyncResult(step.next)
            } else {
                syncRec(step.next, step.depth, headDetector)
            }
        }

        // Head valid y topLevel? corta antes del head
        if (isTopLevelHead(ts, headDetector, depth)) return SyncResult(ts)

        // token normal que no es head -> avanzar 1 y seguir
        val next = advanceOne(ts) ?: return SyncResult(ts)
        return syncRec(next, depth, headDetector)
    }

    private data class SepStep(val next: TokenStream, val depth: Int, val stop: Boolean)

    private fun handleSeparator(tok: SeparatorToken, ts: TokenStream, depth: Int): SepStep = when (tok.separator) {
        Separator.LBRACE -> {
            val next = advanceOne(ts) ?: return SepStep(ts, depth, stop = true)
            SepStep(next, depth + 1, stop = false)
        }
        Separator.RBRACE -> {
            val next = advanceOne(ts) ?: return SepStep(ts, depth, stop = true)
            if (depth == 0) {
                SepStep(next, depth, stop = true)
            } else {
                SepStep(next, depth - 1, stop = false)
            }
        }
        else -> {
            val next = advanceOne(ts) ?: return SepStep(ts, depth, stop = true)
            SepStep(next, depth, stop = false)
        }
    }

    private fun advanceOne(ts: TokenStream): TokenStream? =
        (ts.next() as? Success)?.value?.second

    private fun isTopLevelHead(
        ts: TokenStream,
        headDetector: HeadDetector,
        depth: Int,
    ): Boolean {
        if (depth != 0) return false
        return when (val r = headDetector.detect(ts)) {
            is Success -> r.value !is Unknown
            is Failure -> false
        }
    }
}
