package org.printscript.interpreter.errors

import org.printscript.common.Span

data class ExpectedStringForPrompt(
    override val span: Span,
    val gotType: String,
) : InterpreterError {
    override val message: String =
        "input(...) espera un prompt (string). Recibió $gotType"
}
