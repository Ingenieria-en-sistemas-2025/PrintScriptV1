package org.printscript.ast

import org.printscript.common.LabeledError
import org.printscript.common.Result

interface StatementStream {
    fun next(): Result<Pair<Statement, StatementStream>, LabeledError>
    fun isEof(): Boolean
}
