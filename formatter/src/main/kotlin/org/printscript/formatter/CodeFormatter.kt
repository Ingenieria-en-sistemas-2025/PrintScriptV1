package org.printscript.formatter

import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.formatter.config.FormatterOptions
import org.printscript.formatter.rules.AssignmentSpacingRule
import org.printscript.formatter.rules.BinaryOperatorSpacingRule
import org.printscript.formatter.rules.BlankLinesBeforePrintlnRule
import org.printscript.formatter.rules.ColonSpacingRule
import org.printscript.formatter.rules.MandatorySpacingRule
import org.printscript.formatter.rules.NewlineAfterSemicolonRule
import org.printscript.formatter.rules.WordSpacingRule
import org.printscript.token.EofToken
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
        // Debug detallado para el problema del colon
        if (prev?.codeText == ":" || current.codeText == "string") {
            println("========== DETAILED DEBUG ==========")
            println("prev: '${prev?.codeText}' (${prev?.javaClass?.simpleName})")
            println("current: '${current.codeText}' (${current.javaClass.simpleName})")
            println("next: '${next?.codeText}' (${next?.javaClass?.simpleName})")

            // Probar cada regla individualmente
            val allRules = listOf(
                "MandatorySpacingRule" to MandatorySpacingRule(config),
                "ColonSpacingRule" to ColonSpacingRule(config),
                "AssignmentSpacingRule" to AssignmentSpacingRule(config),
                "BinaryOperatorSpacingRule" to BinaryOperatorSpacingRule(),
                "BlankLinesAfterPrintlnRule" to BlankLinesBeforePrintlnRule(config),
                "NewlineAfterSemicolonRule" to NewlineAfterSemicolonRule(),
                "WordSpacingRule" to WordSpacingRule(),
            )

            allRules.forEach { (name, rule) ->
                val result = rule.apply(prev, current, next)
                if (result != null) {
                    println("  $name -> '$result' (length: ${result.length})")
                } else {
                    println("  $name -> null")
                }
            }

            // Resultado final del registry
            val finalRule = registry.findApplicableRule(prev, current, next)
            println("FINAL RESULT: '$finalRule'")
            println("=====================================")
        }

        val rule = registry.findApplicableRule(prev, current, next)

        val (chunks, st1) = layout.applyPrefix(
            rule,
            prev,
            current,
            next,
            indent.state,
        )
        var lastChar = lastChar
        chunks.forEach { part ->
            lastChar = emitText(out, part, lastChar)
        }
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
        // Apply formatting rules even for EOF to handle final newlines
        val rule = registry.findApplicableRule(prev, eof, null)
        if (rule != null) {
            val (chunks, _) = layout.applyPrefix(rule, prev, eof, null, indent.state)
            chunks.forEach { part -> emitText(out, part, lastChar) }
        }
        return Success(Unit)
    }

    private fun emitText(out: Appendable, text: String, lastChar: Char?): Char? {
        // Si es todo espacios y ya venimos de un espacio, no agregues
        if (text.isNotEmpty() && text.all { it == ' ' } && lastChar == ' ') return lastChar
        // Si es un solo " " y venimos de inicio de línea, no agregues
        if (text == " " && lastChar == '\n') return lastChar

        out.append(text)
        return text.lastOrNull() ?: lastChar
    }
}
