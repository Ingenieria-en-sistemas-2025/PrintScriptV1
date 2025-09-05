import config.FormatterOptions
import rules.AssignmentSpacingRule
import rules.BinaryOperatorSpacingRule
import rules.BlankLinesBeforePrintlnRule
import rules.BlockFormattingRule
import rules.ColonSpacingRule
import rules.FormattingRule
import rules.IfKeywordSpacingRule
import rules.WordSpacingRule

class EnhancedRuleRegistry(config: FormatterOptions) : RuleRegistry {
    private val rules: List<FormattingRule> = listOf(
        ColonSpacingRule(config),
        AssignmentSpacingRule(config),
        BinaryOperatorSpacingRule(),
        IfKeywordSpacingRule(),
        BlankLinesBeforePrintlnRule(config),
        BlockFormattingRule(config),
        WordSpacingRule(),
    )

    override fun findApplicableRule(prev: Token?, current: Token, next: Token?): String? {
        for (rule in rules) {
            val result = rule.apply(prev, current, next)
            if (result != null) return result
        }
        return null
    }
}

/*
listOf(
        ColonSpacingRule(config),
        AssignmentSpacingRule(config),
        BinaryOperatorSpacingRule(),
        BlankLinesBeforePrintlnRule(config),
        NewlineAfterSemicolonRule(),
        WordSpacingRule(),
    )
 */
