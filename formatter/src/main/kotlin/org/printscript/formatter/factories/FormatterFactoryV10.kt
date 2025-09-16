package org.printscript.formatter.factories

import org.printscript.formatter.CodeFormatter
import org.printscript.formatter.Formatter
import org.printscript.formatter.IndentationApplier
import org.printscript.formatter.ListRuleRegistry
import org.printscript.formatter.config.FormatterOptions
import org.printscript.formatter.rules.AssignmentSpacingRule
import org.printscript.formatter.rules.BinaryOperatorSpacingRule
import org.printscript.formatter.rules.BlankLinesBeforePrintlnRule
import org.printscript.formatter.rules.ColonSpacingRule
import org.printscript.formatter.rules.MandatorySpacingRule
import org.printscript.formatter.rules.NewlineAfterSemicolonRule
import org.printscript.formatter.rules.WordSpacingRule

object FormatterFactoryV10 {
    fun create(options: FormatterOptions): Formatter {
        val rules = buildList {
            options.mandatorySingleSpaceSeparation?.let {
                add(MandatorySpacingRule(options))
            }
            if (options.spaceBeforeColonInDecl != null || options.spaceAfterColonInDecl != null) {
                add(ColonSpacingRule(options))
            }
            options.spaceAroundAssignment?.let {
                add(AssignmentSpacingRule(options))
            }
            add(BinaryOperatorSpacingRule())

            options.blankLinesAfterPrintln?.let {
                add(BlankLinesBeforePrintlnRule(options))
            }
            add(NewlineAfterSemicolonRule())
            add(WordSpacingRule())
        }

        val registry = ListRuleRegistry(rules)
        return CodeFormatter(
            config = options,
            registry = registry,
            layout = IndentationApplier(options.indentSpaces),
        )
    }
}
