package org.printscript.lexer.config

import org.printscript.common.Keyword
import org.printscript.common.Operator
import org.printscript.common.Separator
import org.printscript.common.Type
import org.printscript.lexer.lexingrules.BooleanLiteralRule
import org.printscript.lexer.lexingrules.IdentifierOrKeywordRule
import org.printscript.lexer.lexingrules.LexingRule
import org.printscript.lexer.lexingrules.NumberRule
import org.printscript.lexer.lexingrules.OperatorRule
import org.printscript.lexer.lexingrules.RuleKey
import org.printscript.lexer.lexingrules.RuleKeys
import org.printscript.lexer.lexingrules.SeparatorRule
import org.printscript.lexer.lexingrules.StringRule
import org.printscript.lexer.tokencreators.BooleanLiteralCreator
import org.printscript.lexer.tokencreators.IdentifierOrKeywordCreator
import org.printscript.lexer.tokencreators.NumberTokenCreator
import org.printscript.lexer.tokencreators.OperatorTokenCreator
import org.printscript.lexer.tokencreators.SeparatorTokenCreator
import org.printscript.lexer.tokencreators.StringTokenCreator
import org.printscript.lexer.tokencreators.TokenCreator
import org.printscript.lexer.trivia.BlockCommentRule
import org.printscript.lexer.trivia.LineCommentRule
import org.printscript.lexer.trivia.NewLineRule
import org.printscript.lexer.trivia.TriviaRule
import org.printscript.lexer.trivia.WhiteSpaceRule

class PrintScriptv1MapConfig {

    fun keywords(): Map<String, Keyword> = mapOf(
        "let" to Keyword.LET,
        "println" to Keyword.PRINTLN,
        "const" to Keyword.CONST,
        "if" to Keyword.IF,
        "else" to Keyword.ELSE,
        "readInput" to Keyword.READ_INPUT,
        "readEnv" to Keyword.READ_ENV,
    )

    fun types(): Map<String, Type> = mapOf(
        "string" to Type.STRING,
        "number" to Type.NUMBER,
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
        "{" to Separator.LBRACE,
        "}" to Separator.RBRACE,
    )

    fun rules(): List<LexingRule> = listOf(
        BooleanLiteralRule(RuleKeys.BOOLEAN_LITERAL, Regex("""(?:true|false)\b""")),
        StringRule,
        IdentifierOrKeywordRule(RuleKeys.IDENT_OR_KEYWORD, Regex("[A-Za-z_][A-Za-z0-9_]*")),
        NumberRule(RuleKeys.NUMBER, Regex("\\d+(?:\\.\\d+)?")),
        OperatorRule(RuleKeys.OPERATOR, operators()),
        SeparatorRule(RuleKeys.SEPARATOR, separators()),
    )

    fun creators(): Map<RuleKey, TokenCreator> = mapOf(
        RuleKeys.BOOLEAN_LITERAL to BooleanLiteralCreator,
        RuleKeys.STRING to StringTokenCreator,
        RuleKeys.NUMBER to NumberTokenCreator,
        RuleKeys.IDENT_OR_KEYWORD to IdentifierOrKeywordCreator(keywords(), types()),
        RuleKeys.OPERATOR to OperatorTokenCreator(operators()),
        RuleKeys.SEPARATOR to SeparatorTokenCreator(separators()),
    )

    fun triviaRules(): List<TriviaRule> = listOf(
        LineCommentRule,
        BlockCommentRule,
        NewLineRule,
        WhiteSpaceRule,
    )
}
