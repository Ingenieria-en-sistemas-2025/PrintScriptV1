package org.example

import Span

class InternalRuntimeError(override val span: Span, override val message: String): InterpreterError