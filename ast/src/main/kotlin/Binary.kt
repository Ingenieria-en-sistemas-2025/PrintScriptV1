
import Expression
import Operator
data class Binary(
    val left : Expression,
    val right : Expression,
    val operator : Operator
) : Expression