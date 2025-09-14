import ReaderLexTestUtils.lexAllReader
import Utilities.assertLexEqualsWithBuilder
import org.printscript.common.Version
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import kotlin.test.Test
import kotlin.test.assertTrue

class Lexerv1ReaderDslTest {

    private val stress = ReaderLexTestUtils.FeedOptions(maxWindowCapacity = 12, chunkSize = 5, keepTail = 4)

    private fun lexAllTokens(src: String) = lexAllReader(Version.V1, src, stress)
    private fun lexError(src: String) = ReaderLexTestUtils.lexError(Version.V1, src, stress)

    @Test
    fun constBooleanTrueLiteral() = assertLexEqualsWithBuilder(
        src = """const flag: boolean = true;""",
        lexActual = ::lexAllTokens,
    ) {
        kw().const().identifier("flag").sep().colon().ty().booleanType().op().assign().boolean(true).sep().semicolon()
    }

    @Test
    fun ifElseConLlavesYPrintln() = assertLexEqualsWithBuilder(
        src = """if (flag) { println("ok"); } else { println("no"); }""",
        lexActual = ::lexAllTokens,
    ) {
        kw().ifkey().sep().lparen().identifier("flag").sep().rparen()
            .sep().lbrace()
            .kw().println().sep().lparen().string("ok").sep().rparen().sep().semicolon()
            .sep().rbrace()
            .kw().elsekey()
            .sep().lbrace()
            .kw().println().sep().lparen().string("no").sep().rparen().sep().semicolon()
            .sep().rbrace()
    }

    @Test
    fun readInputYReadEnvComoKeywords() = assertLexEqualsWithBuilder(
        src = """
            let name: string = readInput("Name: ");
            let debug: boolean = readEnv("DEBUG");
        """.trimIndent(),
        lexActual = ::lexAllTokens,
    ) {
        kw().let().identifier("name").sep().colon().ty().stringType().op().assign()
            .kw().readInput().sep().lparen().string("Name: ").sep().rparen().sep().semicolon()
            .kw().let().identifier("debug").sep().colon().ty().booleanType().op().assign()
            .kw().readEnv().sep().lparen().string("DEBUG").sep().rparen().sep().semicolon()
    }

    @Test
    fun trueFalseSonLiteralsNoIdentificadores() = assertLexEqualsWithBuilder(
        src = "true false trueValue",
        lexActual = ::lexAllTokens,
    ) {
        boolean(true).boolean(false).identifier("trueValue")
    }

    @Test
    fun simboloDesconocidoDevuelveFailure_v1() {
        val err = lexError("let x = 5 $;")
        assertTrue(err is UnexpectedChar, "Se esperaba UnexpectedChar, fue ${err::class.simpleName}")
    }
}
