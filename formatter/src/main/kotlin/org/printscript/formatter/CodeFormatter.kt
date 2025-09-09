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

    private data class IndentLevelState(val indent: LayoutApplier.State = LayoutApplier.State())
    private data class Accumulator(val chunks: List<String> = emptyList())

    override fun format(ts: TokenStream): Result<String, LabeledError> =
        step(ts, prev = null, indentLevel = IndentLevelState(), acc = Accumulator())

    private fun step(stream: TokenStream, prev: Token?, indentLevel: IndentLevelState, acc: Accumulator): Result<String, LabeledError> =
        stream.peek().flatMap { current -> continueWithPeeked(stream, prev, indentLevel, acc, current) }

    private fun continueWithPeeked(
        stream: TokenStream,
        previousToken: Token?,
        indentLevelState: IndentLevelState,
        accumulator: Accumulator,
        currentToken: Token,
    ): Result<String, LabeledError> =
        if (currentToken is EofToken) {
            finish(previousToken, accumulator, currentToken)
        } else {
            stream.peek(1).flatMap { lookaheadToken ->
                val nextToken = lookaheadToken.takeUnless { it is EofToken }
                val (updatedAcc, updatedIndentLevel) =
                    processToken(previousToken, currentToken, nextToken, indentLevelState, accumulator)
                stream.next().flatMap { (_, nextStream) ->
                    step(nextStream, currentToken, updatedIndentLevel, updatedAcc)
                }
            }
        }

    private fun processToken(
        prev: Token?,
        current: Token,
        next: Token?,
        indentLevel: IndentLevelState,
        acc: Accumulator,
    ): Pair<Accumulator, IndentLevelState> {
        // aplico prefijo, emito el token, ajusto indentacion
        val (chunks, identLevel1) = layout.applyPrefix(registry.findApplicableRule(prev, current, next), prev, current, next, indentLevel.indent)
        val acc1 = appendChunks(acc, chunks)
        val acc2 = emitText(acc1, current.codeText)
        val indentLevel2 = indentLevel.copy(indent = layout.updateAfter(prev, current, identLevel1))
        return acc2 to indentLevel2
    }

    // ultima chance para prefijos (ej: \n)
    private fun finish(prev: Token?, acc: Accumulator, eof: EofToken): Result<String, LabeledError> {
        val (chunks, _) = layout.applyPrefix(registry.findApplicableRule(prev, eof, null), prev, eof, null, IndentLevelState().indent)
        val acc1 = appendChunks(acc, chunks)
        val acc2 = if (prev.isRbrace()) emitText(acc1, "\n") else acc1
        return Success(acc2.chunks.joinToString(""))
    }

    // si es un espacio llama a safeSpace, sino agrega CodeText
    private fun emitText(acc: Accumulator, text: String): Accumulator =
        if (text == " ") safeSpace(acc) else acc.copy(chunks = acc.chunks + text)

    // repite emitText por cada pedacito del prefijo
    private fun appendChunks(accumulator: Accumulator, parts: List<String>): Accumulator =
        parts.fold(accumulator) { acc, part -> emitText(acc, part) }

    // evitar doble espacio
    private fun safeSpace(acc: Accumulator): Accumulator =
        if (lastChar(acc) == ' ' || lastChar(acc) == '\n') acc else acc.copy(chunks = acc.chunks + " ")

    private fun lastChar(acc: Accumulator): Char? = acc.chunks.lastOrNull()?.lastOrNull()
}

private fun Token?.isSeparator(kind: Separator) =
    this is SeparatorToken && this.separator == kind
private fun Token?.isRbrace() = this.isSeparator(Separator.RBRACE)
