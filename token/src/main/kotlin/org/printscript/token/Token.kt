package org.printscript.token

import org.printscript.common.Span

sealed interface Token {
    val span: Span
}
