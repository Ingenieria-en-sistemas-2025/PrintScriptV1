package org.printscript.runner

import org.printscript.analyzer.StreamingAnalyzer
import org.printscript.ast.StatementStream
import org.printscript.common.Version
import org.printscript.formatter.Formatter
import org.printscript.interpreter.InputProvider
import org.printscript.interpreter.Interpreter
import org.printscript.parser.Parser
import org.printscript.token.TokenStream
import java.io.Reader

data class LanguageWiring(
    val version: Version,
    // val tokenStreamFromSource: (String) -> TokenStream,
    val tokenStreamFromReader: (Reader) -> TokenStream,
    val parser: Parser,
    val analyzer: StreamingAnalyzer,
    val formatter: Formatter,
    val interpreterFor: (InputProvider?) -> Interpreter,
    val statementStreamFromTokens: (TokenStream) -> StatementStream,
)
