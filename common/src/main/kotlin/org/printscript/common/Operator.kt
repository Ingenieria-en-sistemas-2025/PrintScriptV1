package org.printscript.common

enum class Operator(val symbol: String) {
    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    UNKNOWN("<?>"),
}
