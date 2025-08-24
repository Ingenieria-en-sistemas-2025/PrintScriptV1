package org.example

import Span

data class RuntimeError(val span: Span?, override val message: String)
    : RuntimeException(message)
