package org.printscript.formatter

import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.token.TokenStream

interface Formatter {
    fun format(ts: TokenStream, out: Appendable): Result<Unit, LabeledError>
}
