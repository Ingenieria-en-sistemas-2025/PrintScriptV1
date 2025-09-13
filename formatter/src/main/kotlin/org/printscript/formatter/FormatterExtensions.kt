package org.printscript.formatter

import org.printscript.common.LabeledError
import org.printscript.common.Result

// sirve para tests
fun Formatter.formatToString(ts: org.printscript.token.TokenStream): Result<String, LabeledError> {
    val sb = StringBuilder()
    return format(ts, sb).map { sb.toString() }
}
