class PrintScriptMapConfig {

    fun keywords(): Map<String, Keyword> = mapOf(
        "let" to Keyword.LET,
        "println" to Keyword.PRINTLN,
    )

    fun types(): Map<String, Type> = mapOf(
        "string" to Type.STRING,
        "number" to Type.NUMBER,
    )

    fun operators(): Map<String, Operator> = mapOf(
        "=" to Operator.ASSIGN,
        "+" to Operator.PLUS,
        "-" to Operator.MINUS,
        "*" to Operator.MULTIPLY,
        "/" to Operator.DIVIDE,
    )

    fun separators(): Map<String, Separator> = mapOf(
        "(" to Separator.LPAREN,
        ")" to Separator.RPAREN,
        ";" to Separator.SEMICOLON,
        ":" to Separator.COLON,
    )

    fun rules(): List<LexingRule> = listOf(
        IdentifierOrKeywordRule(keywords(), types()),
        NumberRule(),
        StringRule,
        OperatorRule(operators()),
        SeparatorRule(separators()),
    )

    fun triviaRules(): List<TriviaRule> = listOf(
        BlockCommentRule,
        LineCommentRule,
        WhiteSpaceRule,
    )
}
