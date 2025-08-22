package rules
import Token

interface FormattingRule {
    fun apply(prev: Token?, current: Token, next: Token?): String?
}