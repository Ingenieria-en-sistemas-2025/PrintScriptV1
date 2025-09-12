package org.printscript.interpreter

sealed interface Value {
    data class Num(val n: Double) : Value
    data class Str(val s: String) : Value
    data class Bool(val b: Boolean) : Value
}
