import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.printscript.ast.Println
import org.printscript.ast.Step
import org.printscript.ast.VarDeclaration
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.parser.ParseOne
import org.printscript.parser.Recovery
import org.printscript.parser.expr.ExprPratt
import org.printscript.parser.factories.GlobalParserFactory
import org.printscript.parser.head.FirstHeadDetector
import org.printscript.parser.head.Head
import org.printscript.parser.head.HeadDetector
import org.printscript.parser.stmt.StmtParser
import org.printscript.token.TestUtils
import org.printscript.token.TokenStream
import org.printscript.token.dsl.kw
import org.printscript.token.dsl.op
import org.printscript.token.dsl.sep
import org.printscript.token.dsl.ty
import kotlin.test.assertEquals

class IfParserTests {

    private fun assertItem(s: org.printscript.ast.StatementStream): Pair<org.printscript.ast.Statement, org.printscript.ast.StatementStream> =
        when (val st = s.nextStep()) {
            is Step.Item -> st.statement to st.next
            is Step.Error -> error("Esperaba Item, vino Error: ${st.error.message}")
            Step.Eof -> error("Esperaba Item, vino EOF")
        }

    @Test
    fun testRecoveryConsumeRbrace() {
        val headDetector = FirstHeadDetector()
        val ts: TokenStream = TestUtils.tokens {
            sep().rbrace()
                .kw().let().identifier("z").sep().colon().ty().numberType().op().assign().number("9").sep().semicolon()
        }

        val sync = Recovery.syncToNextHeadTopLevel(ts, headDetector)
        val parser = GlobalParserFactory.forVersion(Version.V1)!!
        val stream = parser.parse(sync.next)

        val (stmtZ, afterZ) = assertItem(stream)
        assertTrue(stmtZ is VarDeclaration)
        assertEquals("z", (stmtZ as VarDeclaration).name)
        assertTrue(afterZ.nextStep() is Step.Eof)
    }

    @Test
    fun testRecoveryExitsOnRbraceAndResumesAtTopLevelHead() {
        val headDetector = FirstHeadDetector()
        val ts: TokenStream = TestUtils.tokens {
            sep().lbrace()
                .identifier("hello")
                .sep().rbrace()
                .kw().let().identifier("x").sep().colon().ty().numberType()
                .op().assign().number("5").sep().semicolon()
        }

        val sync = Recovery.syncToNextHeadTopLevel(ts, headDetector)
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val stream = parser.parse(sync.next)

        val (stmtX, afterX) = assertItem(stream)
        assertTrue(stmtX is VarDeclaration)
        assertEquals("x", (stmtX as VarDeclaration).name)
        assertTrue(afterX.nextStep() is Step.Eof)
    }

    @Test
    fun testRecoveryAdvancesWhenAlreadyAtTopLevelHead() {
        val headDetector = FirstHeadDetector()
        val ts = TestUtils.tokens {
            kw().let().identifier("a").sep().colon().ty().numberType()
                .op().assign().number("1") // <--- falta ';'
            kw().println().sep().lparen().string("x").sep().rparen().sep().semicolon()
        }
        val sync = Recovery.syncToNextHeadTopLevel(ts, headDetector)
        val parser = GlobalParserFactory.forVersion(Version.V0)!!
        val stream = parser.parse(sync.next)

        val (stmt, after) = assertItem(stream)
        assertTrue(stmt is Println)
        assertTrue(after.nextStep() is Step.Eof)
    }

    @Test
    fun testParseOneReturnsFailureOnEof() {
        val dummyHeadDetector = object : HeadDetector {
            override fun detect(ts: TokenStream): Result<Head, LabeledError> {
                throw AssertionError("HeadDetector.detect no debería llamarse en EOF")
            }
        }
        val dummyStmtParsers: Map<Head, StmtParser> = emptyMap()
        val parseOne = ParseOne.make(dummyHeadDetector, dummyStmtParsers, ExprPratt(emptyMap(), emptyMap(), emptyMap(), emptyMap()))
        val eofTs: TokenStream = TestUtils.tokens { this }

        val res = parseOne(eofTs)
        assertTrue(res is Failure, "Esperaba Failure en EOF")
        val err = (res as Failure).error
        assertTrue(err.message.contains("EOF inesperado"))
    }
}
