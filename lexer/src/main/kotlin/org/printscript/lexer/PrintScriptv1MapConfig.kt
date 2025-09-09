package org.printscript.lexer

import org.printscript.common.Keyword
import org.printscript.common.Operator
import org.printscript.common.Separator
import org.printscript.common.Type

class PrintScriptv1MapConfig {

    fun keywords(): Map<String, Keyword> = mapOf(
        "let" to Keyword.LET,
        "println" to Keyword.PRINTLN,
        // 1.1
        "const" to Keyword.CONST,
        "if" to Keyword.IF,
        "else" to Keyword.ELSE,
        "readInput" to Keyword.READ_INPUT,
        "readEnv" to Keyword.READ_ENV,
    )

    fun types(): Map<String, Type> = mapOf(
        "string" to Type.STRING,
        "number" to Type.NUMBER,
        // 1.1
        "boolean" to Type.BOOLEAN,
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
        // 1.1
        "{" to Separator.LBRACE,
        "}" to Separator.RBRACE,
    )

    fun rules(): List<LexingRule> = listOf(
        // 1.1: primero los literales booleanos para que no salgan como identificadores
        BooleanLiteralRule,
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
