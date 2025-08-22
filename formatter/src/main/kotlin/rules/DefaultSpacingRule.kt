package rules

import Separator
import SeparatorToken
import Token
import WordLikeToken
import Operator
import OperatorToken
import config.FormatterConfig

class DefaultSpacingRule(private val config: FormatterConfig) : FormattingRule {

    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (prev == null || current == null) return null

        // Nunca espacio antes de ), ; o :
        if (current is SeparatorToken && current.separator in setOf(Separator.RPAREN, Separator.SEMICOLON, Separator.COLON)) return null

        // Nunca espacio inmediatamente despues de (
        if (prev is SeparatorToken && prev.separator == Separator.LPAREN) return null

        // No espacio entre palabra/keyword y (
        if (prev is WordLikeToken &&
            current is SeparatorToken && current.separator == Separator.LPAREN
        ) return null

        // Si venimos de :
        if (prev is SeparatorToken && prev.separator == Separator.COLON) {
            if (!config.spaceAfterColonInDecl) return null
            // Si fuera true, ColonSpacingRule ya metio el espacio
            return null }

        // Si venimos de =
        if (prev is OperatorToken && prev.operator == Operator.ASSIGN) {
            if (!config.spaceAroundAssignment) return null
            // Si fuera true, AssignmentSpacingRule ya metio el espacio
            return null
        }

        // Regla general: si alguno es palabra, agregar un espacio
        return if (prev is WordLikeToken || current is WordLikeToken) " " else null
    }
}
