package org.printscript.lexer.config

import org.printscript.common.Keyword
import org.printscript.common.Operator
import org.printscript.common.Separator
import org.printscript.common.Type
import org.printscript.lexer.lexingrules.IdentifierOrKeywordRule
import org.printscript.lexer.lexingrules.LexingRule
import org.printscript.lexer.lexingrules.NumberRule
import org.printscript.lexer.lexingrules.OperatorRule
import org.printscript.lexer.lexingrules.SeparatorRule
import org.printscript.lexer.lexingrules.StringRule
import org.printscript.lexer.triviarules.BlockCommentRule
import org.printscript.lexer.triviarules.LineCommentRule
import org.printscript.lexer.triviarules.TriviaRule
import org.printscript.lexer.triviarules.WhiteSpaceRule

class PrintScriptv0MapConfig {

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
