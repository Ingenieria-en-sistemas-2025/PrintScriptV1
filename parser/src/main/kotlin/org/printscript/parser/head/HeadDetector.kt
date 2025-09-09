package org.printscript.parser.head

import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.token.TokenStream

interface HeadDetector {
    fun detect(tokenStream: TokenStream): Result<Head, LabeledError>
}
