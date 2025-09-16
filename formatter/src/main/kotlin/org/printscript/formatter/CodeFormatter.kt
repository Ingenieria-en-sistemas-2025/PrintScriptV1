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
) : Formatter {

    private data class IndentState(val state: LayoutApplier.State = LayoutApplier.State())

    private data class TriviaContext(val chunks: List<String> = emptyList()) {
        fun add(text: String): TriviaContext = copy(chunks = chunks + text)
        fun lastOrNull(): String? = chunks.lastOrNull()
        fun isNotEmpty(): Boolean = chunks.isNotEmpty()
        fun anyNewline(): Boolean = chunks.any { it.contains('\n') }
        inline fun forEach(action: (String) -> Unit) = chunks.forEach(action)
    }

    private data class StepOut(
        val indent: IndentState,
        val lastChar: Char?,
        val trivia: TriviaContext,
    )

    override fun format(ts: TokenStream, out: Appendable): Result<Unit, LabeledError> =
        step(ts, prev = null, indent = IndentState(), trivia = TriviaContext(), out = out, lastChar = null)

    private fun step(
        stream: TokenStream,
        prev: Token?,
        indent: IndentState,
        trivia: TriviaContext,
        out: Appendable,
        lastChar: Char?,
    ): Result<Unit, LabeledError> =
        stream.peek().flatMap { cur ->
            if (cur is EofToken) {
                finish(prev, indent, trivia, out, lastChar, cur)
            } else {
                stream.peek(1).flatMap { lookahead ->
                    val next = lookahead.takeUnless { it is EofToken }
                    val (indent2, lastChar2, trivia2) = process(prev, cur, next, indent, trivia, out, lastChar)
                    stream.next().flatMap { (_, ns) -> step(ns, cur, indent2, trivia2, out, lastChar2) }
                }
            }
        }

    private fun process(
        prev: Token?,
        current: Token,
        next: Token?,
        indent: IndentState,
        trivia: TriviaContext,
        out: Appendable,
        lastChar: Char?,
    ): StepOut {
        if (current is TriviaToken) {
            return handleTrivia(prev, current, indent, trivia, lastChar)
        }

        val normalizedTrivia = if (current is KeywordToken && current.kind == Keyword.PRINTLN) {
            maybeNormalizeTriviaAfterPrintln(trivia)
        } else {
            trivia
        }

        // Aplica regla (si hay) o emite token + indentación post '\n' de la trivia
        return applyRuleOrEmitRaw(prev, current, next, indent, normalizedTrivia, out, lastChar)
    }

    private fun finish(
        prev: Token?,
        indent: IndentState,
        trivia: TriviaContext,
        out: Appendable,
        lastChar: Char?,
        eof: EofToken,
    ): Result<Unit, LabeledError> {
        var currentLastChar = lastChar
        trivia.forEach { triviaText ->
            currentLastChar = emitText(out, triviaText, currentLastChar)
        }

        // Regla de cierre (p.ej., forzar \n final)
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

    private fun handleTrivia(
        prev: Token?,
        current: TriviaToken,
        indent: IndentState,
        trivia: TriviaContext,
        lastChar: Char?,
    ): StepOut {
        if (current is WhitespaceToken && prev?.codeText == "=" && config.spaceAroundAssignment == false) {
            val st = layout.updateAfter(prev, current, indent.state)
            return StepOut(IndentState(st), lastChar, trivia)
        }

        val triviaText = when (current) {
            is WhitespaceToken -> " "
            is NewlineToken -> "\n"
            is LineCommentToken -> current.raw
            is BlockCommentToken -> current.raw
            else -> current.codeText
        }

        val st = layout.updateAfter(prev, current, indent.state)
        return StepOut(IndentState(st), lastChar, trivia.add(triviaText))
    }

    // compacta trivia alrededor de PRINTLN, deja solo el ultimo
    private fun maybeNormalizeTriviaAfterPrintln(trivia: TriviaContext): TriviaContext {
        if (config.blankLinesAfterPrintln != null && trivia.chunks.size > 1) {
            val last = trivia.lastOrNull()
            return if (last != null) TriviaContext(listOf(last)) else TriviaContext()
        }
        return trivia
    }

    private fun applyRuleOrEmitRaw(
        prev: Token?,
        current: Token,
        next: Token?,
        indent: IndentState,
        trivia: TriviaContext,
        out: Appendable,
        lastChar: Char?,
    ): StepOut {
        val rule = registry.findApplicableRule(prev, current, next)
        var lastCh = lastChar

        if (rule == null) {
            // Emitir trivia acumulada
            trivia.forEach { triviaText ->
                lastCh = emitText(out, triviaText, lastCh)
            }
            // si hubo al menos un \n en la trivia, aplicar indentacion
            if (trivia.isNotEmpty() && trivia.anyNewline()) {
                lastCh = emitText(out, layout.spacing(indent.state), lastCh)
            }
            lastCh = emitText(out, current.codeText, lastCh)

            val st = layout.updateAfter(prev, current, indent.state)
            return StepOut(IndentState(st), lastCh, TriviaContext())
        }

        // Si hay regla, descartar trivia previa y aplica prefijo del layout
        val (chunks, st1) = layout.applyPrefix(rule, prev, current, next, indent.state)
        chunks.forEach { part -> lastCh = emitText(out, part, lastCh) }

        lastCh = emitText(out, current.codeText, lastCh)

        val st2 = layout.updateAfter(prev, current, st1)
        return StepOut(IndentState(st2), lastCh, TriviaContext())
    }
}
