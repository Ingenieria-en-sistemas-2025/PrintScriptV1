package head

import LabeledError
import Result
import TokenStream

interface HeadDetector {
    fun detect(tokenStream: TokenStream): Result<Head, LabeledError>
}
