package org.printscript.runner

import org.printscript.analyzer.AnalyzerFactory
import org.printscript.ast.StatementStream
import org.printscript.common.Version
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterOptions
import org.printscript.formatter.factories.GlobalFormatterFactory
import org.printscript.interpreter.GlobalInterpreterFactory
import org.printscript.interpreter.InputProvider
import org.printscript.interpreter.Interpreter
import org.printscript.lexer.config.LexerFactory
import org.printscript.parser.factories.GlobalParserFactory
import org.printscript.token.TokenStream
import java.io.Reader

object LanguageWiringFactory {
    fun forVersion(version: Version, formatterOptions: FormatterOptions = FormatterConfig()): LanguageWiring {
        val lexerFactory = LexerFactory()
        val tsFromSource: (String) -> TokenStream = { src -> lexerFactory.tokenStream(version, src) }
        val tsFromReader: (Reader) -> TokenStream = { r -> lexerFactory.tokenStream(version, r) }

        val parser = GlobalParserFactory.forVersion(version)
            ?: error("Parser not available for version $version")

        val analyzer = AnalyzerFactory.forVersion(version)

        val formatter = GlobalFormatterFactory
            .forVersion(version, formatterOptions) ?: error("Formatter not available for version $version")

        val interpreterFor: (InputProvider?) -> Interpreter = { inputOverride ->
            GlobalInterpreterFactory.forVersion(version, inputOverride)
        }

        val stmtStreamFromTokens: (TokenStream) -> StatementStream = parser::parse

        return LanguageWiring(
            version = version,
            tokenStreamFromSource = tsFromSource,
            tokenStreamFromReader = tsFromReader,
            parser = parser,
            analyzer = analyzer,
            formatter = formatter,
            interpreterFor = interpreterFor,
            statementStreamFromTokens = stmtStreamFromTokens,
        )
    }
}

// elige la func correcta del wiring y construye el TokenStream con el lexer
internal fun tokenStream(io: ProgramIo, w: LanguageWiring): TokenStream =
    io.source?.let(w.tokenStreamFromSource) ?: w.tokenStreamFromReader(io.reader!!)
