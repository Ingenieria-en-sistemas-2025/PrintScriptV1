package head

import head.Head
import parser.TokenStream

interface HeadDetector {
    fun detect(tokenStream: TokenStream): Head
}