package org.printscript.parser.expr

private const val LOWEST = 1
private const val ADD = 10
private const val MUL = 20

enum class Prec(val priority: Int) {
    LOWEST(org.printscript.parser.expr.LOWEST),
    ADD(org.printscript.parser.expr.ADD), // +, -
    MUL(org.printscript.parser.expr.MUL), // *, /
}
