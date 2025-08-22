import config.FormatterConfig
import rules.AssignmentSpacingRule
import rules.BinaryOperatorSpacingRule
import rules.BlankLinesBeforePrintlnRule
import rules.ColonSpacingRule
import rules.FormattingRule
import rules.NewlineAfterSemicolonRule

class DefaultRuleRegistry(config: FormatterConfig) : RuleRegistry {
    private val rules: List<FormattingRule> = listOf(
        ColonSpacingRule(config),
        AssignmentSpacingRule(config),
        BinaryOperatorSpacingRule(),
        NewlineAfterSemicolonRule(),
        BlankLinesBeforePrintlnRule(config),
    )

    override fun findApplicableRule(prev: Token?, current: Token, next: Token?): String? {
        for (rule in rules) {
            val out = rule.apply(prev, current, next)
            if (out != null) return out
        }
        return null
    }
}