package dsl

fun TokenBuilder.kw() = KeywordDsl(this)
class KeywordDsl(private val b: TokenBuilder) {
    fun let() = b.keyword(Keyword.LET)
    fun const() = b.keyword(Keyword.CONST)
    fun ifkey() = b.keyword(Keyword.IF)
    fun elsekey() = b.keyword(Keyword.ELSE)
    fun println() = b.keyword(Keyword.PRINTLN)
    fun readInput() = b.keyword(Keyword.READ_INPUT)
    fun readEnv() = b.keyword(Keyword.READ_ENV)
}
