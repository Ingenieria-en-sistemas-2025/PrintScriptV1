package org.printscript.ast

import org.printscript.common.LabeledError

sealed interface Step {
    data class Item(val statement: Statement, val next: StatementStream) : Step
    data class Error(val error: LabeledError, val next: StatementStream) : Step
    data object Eof : Step
}
