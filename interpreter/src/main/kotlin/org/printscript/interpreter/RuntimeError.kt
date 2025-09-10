package org.printscript.interpreter

import org.printscript.common.Span

data class RuntimeError(val span: Span?, override val message: String) :
    RuntimeException(message)
