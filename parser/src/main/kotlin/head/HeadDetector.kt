package head

import TokenStream

interface HeadDetector {
    fun detect(tokenStream: TokenStream): Head
}
