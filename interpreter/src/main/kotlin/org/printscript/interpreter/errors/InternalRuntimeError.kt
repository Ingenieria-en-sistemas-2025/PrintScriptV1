package org.printscript.interpreter.errors

import org.printscript.common.Span

class InternalRuntimeError(override val span: Span, override val message: String) : InterpreterError
