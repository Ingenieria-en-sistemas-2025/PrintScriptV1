import config.FormatterOptions
import rules.AssignmentSpacingRule
import rules.BinaryOperatorSpacingRule
import rules.BlankLinesBeforePrintlnRule
import rules.ColonSpacingRule
import rules.FormattingRule
import rules.NewlineAfterSemicolonRule
import rules.WordSpacingRule

class DefaultRuleRegistry(config: FormatterOptions) : RuleRegistry {
    private val rules: List<FormattingRule> = listOf(
        ColonSpacingRule(config),
        AssignmentSpacingRule(config),
        BinaryOperatorSpacingRule(),
        BlankLinesBeforePrintlnRule(config),
        NewlineAfterSemicolonRule(),
        WordSpacingRule(),
    )

    override fun findApplicableRule(prev: Token?, current: Token, next: Token?): String? {
        for (rule in rules) {
            val string = rule.apply(prev, current, next)
            if (string != null) return string
        }
        return null
    }
}
