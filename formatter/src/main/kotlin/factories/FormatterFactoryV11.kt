package factories

import CodeFormatter
import Formatter
import IndentationApplier
import ListRuleRegistry
import config.FormatterOptions
import rules.AssignmentSpacingRule
import rules.BinaryOperatorSpacingRule
import rules.BlankLinesBeforePrintlnRule
import rules.BlockFormattingRule
import rules.ColonSpacingRule
import rules.FormattingRule
import rules.IfKeywordSpacingRule
import rules.WordSpacingRule

object FormatterFactoryV11 {
    fun create(options: FormatterOptions): Formatter {
        val rules: List<FormattingRule> = listOf(
            ColonSpacingRule(options),
            AssignmentSpacingRule(options),
            BinaryOperatorSpacingRule(),
            IfKeywordSpacingRule(),
            BlankLinesBeforePrintlnRule(options),
            BlockFormattingRule(),
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
