package org.printscript.lexer

import org.printscript.common.Span
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.lexer.lexingrules.LexingRule

class LongestMatchTokenMatcher(
    private val rules: List<LexingRule>,
    private val initialProbe: Int = DEFAULT_INITIAL_PROBE, // tamaño inicial del slice
    private val maxProbe: Int = DEFAULT_MAX_PROBE, // cota superior del slice
) : TokenMatcher {

    companion object {
        const val DEFAULT_INITIAL_PROBE: Int = 32
        const val DEFAULT_MAX_PROBE: Int = 1 shl 20 // 1 MiB (en chars)
        private const val PROBE_GROWTH_FACTOR: Int = 2
    }

    override fun matchNext(scanner: Scanner): Match {
        val startPos = scanner.position()
        val best = BestMatch()

        // Recorremos “probes” crecientes hasta que ya no tenga sentido seguir.
        scanWithIncreasingProbe(scanner, initialProbe, best)

        if (best.len > 0 && best.rule != null) {
            return Match.Success(best.rule!!.key, startPos, best.len)
        }

        // Ninguna regla matcheó => UnexpectedChar con avance de 1
        val after = scanner.advance(1)
        return Match.Failure(UnexpectedChar(Span(startPos, after.position()), scanner.peek()))
    }

    private tailrec fun scanWithIncreasingProbe(
        scanner: Scanner,
        probe: Int,
        best: BestMatch,
    ) {
        val slice = scanner.slice(probe)
        val sliceLen = slice.length
        val hitBoundary = evaluateRulesOn(slice, probe, best)
        val reachedEof = (sliceLen < probe)

        if (!shouldContinue(hitBoundary, reachedEof, probe)) return
        scanWithIncreasingProbe(scanner, nextProbeSize(probe), best)
    }

    private fun evaluateRulesOn(
        slice: CharSequence,
        requestedProbe: Int,
        best: BestMatch,
    ): Boolean {
        var hitBoundary = false
        val sliceLen = slice.length

        for (rule in rules) {
            val len = rule.matchLength(slice)
            if (len > best.len) {
                best.len = len
                best.rule = rule
            }
            if (len == sliceLen && sliceLen == requestedProbe) {
                hitBoundary = true
            }
        }
        return hitBoundary
    }

    // Estado mutable y local para acumular el mejor match
    private data class BestMatch(var len: Int = 0, var rule: LexingRule? = null)

    private fun shouldContinue(hitBoundary: Boolean, reachedEof: Boolean, probe: Int): Boolean =
        hitBoundary && !reachedEof && probe < maxProbe

    private fun nextProbeSize(current: Int): Int =
        minOf(current * PROBE_GROWTH_FACTOR, maxProbe)
}
