package org.printscript.interpreter

import org.printscript.common.Type

// ("a" -> org.printscript.interpreter.Binding(type = NUMBER, value = Num(12.0)))
data class Binding(val type: Type, val value: Value, val isConst: Boolean)
