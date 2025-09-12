package org.printscript.formatter

import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Separator
import org.printscript.common.Success
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.EofToken
import org.printscript.token.SeparatorToken
import org.printscript.token.Token
import org.printscript.token.TokenStream
import org.printscript.token.codeText

class CodeFormatter(
    private val config: FormatterOptions,
    private val registry: RuleRegistry,
    private val layout: LayoutApplier = IndentationApplier(config.indentSpaces),
) : Formatter {

    private data class IndentState(val state: LayoutApplier.State = LayoutApplier.State())

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
                stream.peek(1).flatMap { la ->
                    val next = la.takeUnless { it is EofToken }
                    val (indent2, lastChar2) = process(prev, cur, next, indent, out, lastChar)
                    stream.next().flatMap { (_, ns) -> step(ns, cur, indent2, out, lastChar2) }
                }
            }
        }

    @Suppress("LongParameterList")
    private fun process(
        prev: Token?,
        current: Token,
        next: Token?,
        indent: IndentState,
        out: Appendable,
        lastChar: Char?,
    ): Pair<IndentState, Char?> {
        val (chunks, st1) = layout.applyPrefix(
            registry.findApplicableRule(prev, current, next),
            prev,
            current,
            next,
            indent.state,
        )
        var lastChar = lastChar
        chunks.forEach { part -> lastChar = emitText(out, part, lastChar) }
        lastChar = emitText(out, current.codeText, lastChar)
        val st2 = layout.updateAfter(prev, current, st1)
        return IndentState(st2) to lastChar
    }

    private fun finish(
        prev: Token?,
        indent: IndentState,
        out: Appendable,
        lastChar: Char?,
        eof: EofToken,
    ): Result<Unit, LabeledError> {
        val (chunks, _) = layout.applyPrefix(
            registry.findApplicableRule(prev, eof, null),
            prev,
            eof,
            null,
            indent.state,
        )
        var lastChar = lastChar
        chunks.forEach { part -> lastChar = emitText(out, part, lastChar) }
        if (prev.isRbrace()) lastChar = emitText(out, "\n", lastChar)
        return Success(Unit)
    }

    private fun emitText(out: Appendable, text: String, lastChar: Char?): Char? {
        if (text == " ") {
            if (lastChar == ' ' || lastChar == '\n') return lastChar
        }
        out.append(text)
        return text.lastOrNull() ?: lastChar
    }
}

private fun Token?.isSeparator(kind: Separator) =
    this is SeparatorToken && this.separator == kind
private fun Token?.isRbrace() = this.isSeparator(Separator.RBRACE)
