package factories

import CodeFormatter
import Formatter
import IndentationApplier
import ListRuleRegistry
import config.FormatterOptions
import rules.AssignmentSpacingRule
import rules.BinaryOperatorSpacingRule
import rules.BlankLinesBeforePrintlnRule
import rules.ColonSpacingRule
import rules.FormattingRule
import rules.NewlineAfterSemicolonRule
import rules.WordSpacingRule

object FormatterFactoryV10 {
    fun create(options: FormatterOptions): Formatter {
        val rules: List<FormattingRule> = listOf(
            ColonSpacingRule(options),
            AssignmentSpacingRule(options),
            BinaryOperatorSpacingRule(),
            BlankLinesBeforePrintlnRule(options),
            NewlineAfterSemicolonRule(),
            WordSpacingRule(),
        )
        val registry = ListRuleRegistry(rules)
        return CodeFormatter(
            config = options,
            registry = registry,
            layout = IndentationApplier(options.indentSpaces),
        )
    }
}
