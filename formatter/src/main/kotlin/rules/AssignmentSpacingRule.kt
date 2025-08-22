package rules

import OperatorToken
import Token
import config.FormatterConfig

class AssignmentSpacingRule (private val formatterConfig: FormatterConfig): FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is OperatorToken && current.operator == Operator.ASSIGN){
            return if(formatterConfig.spaceAroundAssignment) " ${current.operator.symbol} " else current.operator.symbol
        }
        return null
    }
}
// Mira el token actual
// Si el token es un operador y es =
// Si formatterConfig.spaceAroundAssignment == true, devuelve " = "
// Si es false, devuelve "=" (sin espacios)
// Si no es = , devuelve null para que otras reglas (o el default) decidan.