package expr

private const val LOWEST = 1
private const val ADD = 10
private const val MUL = 20

enum class Prec(val priority: Int) {
    LOWEST(expr.LOWEST),
    ADD(expr.ADD), // +, -
    MUL(expr.MUL), // *, /
}
