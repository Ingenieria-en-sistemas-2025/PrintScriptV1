import rules.FormattingRule

class ListRuleRegistry(
    private val rules: List<FormattingRule>,
) : RuleRegistry {
    override fun findApplicableRule(prev: Token?, current: Token, next: Token?): String? {
        for (rule in rules) {
            val r = rule.apply(prev, current, next)
            if (r != null) return r
        }
        return null
    }
}
