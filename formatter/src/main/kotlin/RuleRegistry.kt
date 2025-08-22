interface RuleRegistry {
    fun findApplicableRule(prev: Token?, current: Token, next: Token?): String?
}
