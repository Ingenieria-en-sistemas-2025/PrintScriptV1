package org.printscript.formatter

import org.printscript.common.Keyword
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.BlockCommentToken
import org.printscript.token.EofToken
import org.printscript.token.KeywordToken
import org.printscript.token.LineCommentToken
import org.printscript.token.NewlineToken
import org.printscript.token.Token
import org.printscript.token.TokenStream
import org.printscript.token.TriviaToken
import org.printscript.token.WhitespaceToken
import org.printscript.token.codeText

class CodeFormatter(
    private val config: FormatterOptions,
    private val registry: RuleRegistry,
    private val layout: LayoutApplier = IndentationApplier(config.indentSpaces),
    private val triviaContext: MutableList<String> = mutableListOf(),
) : Formatter {

    private data class IndentState(val state: LayoutApplier.State = LayoutApplier.State())
    private data class StepOut(val indent: IndentState, val lastChar: Char?)

    override fun format(ts: TokenStream, out: Appendable): Result<Unit, LabeledError> =
        step(ts, prev = null, indent = IndentState(), out = out, lastChar = null)

    private fun step(
        stream: TokenStream,
        prev: Token?,
        indent: IndentState,
        out: Appendable,
        lastChar: Char?,
    ): Result<Unit, LabeledError> =
        stream.peek().flatMap { cur ->
            if (cur is EofToken) {
                finish(prev, indent, out, lastChar, cur)
            } else {
                stream.peek(1).flatMap { lookahead ->
                    val next = lookahead.takeUnless { it is EofToken }
                    val (indent2, lastChar2) = process(prev, cur, next, indent, out, lastChar)
                    stream.next().flatMap { (_, ns) -> step(ns, cur, indent2, out, lastChar2) }
                }
            }
        }

    private fun process(
        prev: Token?,
        current: Token,
        next: Token?,
        indent: IndentState,
        out: Appendable,
        lastChar: Char?,
    ): Pair<IndentState, Char?> {
        if (current is TriviaToken) {
            val res = handleTrivia(prev, current, indent, lastChar)
            return res.indent to res.lastChar
        }
        if (current is KeywordToken && current.kind == Keyword.PRINTLN) {
            maybeNormalizeTriviaAfterPrintln()
        }

        //  Aplica regla (si hay) o emite token + indentacion post '\n' de la trivia
        val res = applyRuleOrEmitRaw(prev, current, next, indent, out, lastChar)
        return res.indent to res.lastChar
    }

    private fun finish(
        prev: Token?,
        indent: IndentState,
        out: Appendable,
        lastChar: Char?,
        eof: EofToken,
    ): Result<Unit, LabeledError> {
        var currentLastChar = lastChar
        triviaContext.forEach { triviaText ->
            currentLastChar = emitText(out, triviaText, currentLastChar)
        }
        triviaContext.clear()

        val rule = registry.findApplicableRule(prev, eof, null)
        if (rule != null) {
            val (chunks, _) = layout.applyPrefix(rule, prev, eof, null, indent.state)
            chunks.forEach { part -> currentLastChar = emitText(out, part, currentLastChar) }
        }
        return Success(Unit)
    }

    private fun emitText(out: Appendable, text: String, lastChar: Char?): Char? {
        out.append(text)
        return text.lastOrNull() ?: lastChar
    }

    // helper para manejar trivia (espacios, \n, comentarios)
    private fun handleTrivia(
        prev: Token?,
        current: TriviaToken,
        indent: IndentState,
        lastChar: Char?,
    ): StepOut {
        // Caso especial: espacio justo despues de "=" y config NO quiere espacios alrededor del "="
        if (current is WhitespaceToken && prev?.codeText == "=" && config.spaceAroundAssignment == false) {
            val st = layout.updateAfter(prev, current, indent.state)
            return StepOut(IndentState(st), lastChar)
        }

        val triviaText = when (current) {
            is WhitespaceToken -> " "
            is NewlineToken -> "\n"
            is LineCommentToken -> current.raw
            is BlockCommentToken -> current.raw
            else -> current.codeText
        }

        triviaContext.add(triviaText)
        val st = layout.updateAfter(prev, current, indent.state)
        return StepOut(IndentState(st), lastChar)
    }

    //  helper para println (con indent original)
    private fun maybeNormalizeTriviaAfterPrintln() {
        if (config.blankLinesAfterPrintln != null) {
            if (triviaContext.size > 1) {
                // deja solo el ultimo fragmento del trivia
                val last = triviaContext.lastOrNull()
                triviaContext.clear()
                if (last != null) triviaContext.add(last)
            }
        }
    }

    private fun applyRuleOrEmitRaw(
        prev: Token?,
        current: Token,
        next: Token?,
        indent: IndentState,
        out: Appendable,
        lastChar: Char?,
    ): StepOut {
        val rule = registry.findApplicableRule(prev, current, next)
        var lastCh = lastChar

        if (rule == null) {
            triviaContext.forEach { triviaText ->
                lastCh = emitText(out, triviaText, lastCh)
            }

            if (triviaContext.isNotEmpty() && triviaContext.any { it.contains('\n') }) {
                lastCh = emitText(out, layout.spacing(indent.state), lastCh)
            }

            triviaContext.clear()

            lastCh = emitText(out, current.codeText, lastCh)

            val st = layout.updateAfter(prev, current, indent.state)
            return StepOut(IndentState(st), lastCh)
        }

        triviaContext.clear()
        val (chunks, st1) = layout.applyPrefix(rule, prev, current, next, indent.state)
        chunks.forEach { part -> lastCh = emitText(out, part, lastCh) }

        lastCh = emitText(out, current.codeText, lastCh)

        val st2 = layout.updateAfter(prev, current, st1)
        return StepOut(IndentState(st2), lastCh)
    }
}
