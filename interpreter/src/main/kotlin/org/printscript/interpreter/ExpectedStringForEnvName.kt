package org.printscript.interpreter

import org.printscript.common.Span

data class ExpectedStringForEnvName(
    override val span: Span,
    val gotType: String, // "number", "string", etc
) : InterpreterError {
    override val message: String =
        "env(...) espera un nombre (string). Recibió $gotType"
}
