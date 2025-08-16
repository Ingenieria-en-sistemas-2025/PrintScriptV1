
private val IDENT_REGEX = Regex("[A-Za-z_][A-Za-z0-9_]*")

class IdentifierOrKeywordRule(kw: Map<String, Keyword>, tp: Map<String, Type>): LexingRule {

    private val keywords: Map<String, Keyword> = kw.toMap();
    private val types: Map<String, Type> = tp.toMap()


    override fun matchLength(string: String): Int {
        val match = IDENT_REGEX.matchAt(string, 0) ?: return 0;
        return match.value.length
    }

    override fun build(lexeme: String, span: Span): Token {
        types[lexeme]?.let {return TypeToken(it, span)}
        keywords[lexeme]?.let { return KeywordToken(it, span) }
        return IdentifierToken(lexeme, span)
    }
}