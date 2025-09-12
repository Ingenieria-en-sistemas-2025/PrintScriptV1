package org.printscript.lexer.config

import org.printscript.lexer.lexingrules.LexingRule
import org.printscript.lexer.lexingrules.RuleKey
import org.printscript.lexer.tokencreators.TokenCreator
import org.printscript.lexer.triviarules.TriviaRule

data class LexingConfig(
    val rules: List<LexingRule>,
    val trivia: List<TriviaRule>,
    val creators: Map<RuleKey, TokenCreator>,
)
