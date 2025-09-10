package org.printscript.formatter

import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.token.TokenStream

interface Formatter {
    fun format(ts: TokenStream): Result<String, LabeledError>
}
