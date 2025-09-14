import org.junit.jupiter.api.Assertions.assertFalse
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenCollector
import org.printscript.lexer.TokenFactory
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.config.PrintScriptv0MapConfig
import org.printscript.lexer.memory.BufferedStreamingTokenStream
import org.printscript.lexer.trivia.CompositeTriviaSkipper
import org.printscript.token.EofToken
import org.printscript.token.Token
import org.printscript.token.TokenStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class BufferedStreamingTokenStreamTest {

    // ---------- Helpers ----------
    private fun tokenizerV0(src: String): Tokenizer {
        val cfg = PrintScriptv0MapConfig()
        val matcher = LongestMatchTokenMatcher(cfg.rules())
        val skipper = CompositeTriviaSkipper(cfg.triviaRules())
        val factory = TokenFactory(cfg.creators())
        return Tokenizer.of(src, matcher, skipper, factory)
    }

    private fun stream(src: String): TokenStream =
        BufferedStreamingTokenStream.of(tokenizerV0(src))

    private fun <T> assertSuccess(r: Result<T, LabeledError>, msg: String = "Expected Success"): T =
        when (r) {
            is Success -> r.value
            is Failure -> fail("$msg, got Failure: ${r.error.humanReadable()}")
        }

    private fun <T> assertFailure(r: Result<T, LabeledError>, msg: String = "Expected Failure"): LabeledError =
        when (r) {
            is Failure -> r.error
            is Success -> fail("$msg, got Success: ${r.value}")
        }

    @Test
    fun peek_does_not_advance_and_can_lookahead() {
        val ts = stream("x = 1;")

        val t0a = assertSuccess(ts.peek(0))
        assertEquals("ID(x)", t0a.toString())

        val t1 = assertSuccess(ts.peek(1))
        assertEquals("OP(ASSIGN)", t1.toString())

        // peek(0) otra vez → NO avanza
        val t0b = assertSuccess(ts.peek(0))
        assertEquals("ID(x)", t0b.toString())

        // lookahead lejos fuerza producción hasta allí
        val t3 = assertSuccess(ts.peek(3))
        assertEquals("SEP(SEMICOLON)", t3.toString())

        val t4 = assertSuccess(ts.peek(4))
        assertEquals("EOF", t4.toString())
    }

    @Test
    fun next_advances_and_chains_until_eof() {
        val ts0 = stream("let a: number = 1;")
        assertFalse(ts0.isEof())

        val (tok0, ts1) = assertSuccess(ts0.next()) // KW(LET)
        assertEquals("KW(LET)", tok0.toString())
        assertFalse(ts1.isEof())

        val (tok1, ts2) = assertSuccess(ts1.next()) // ID(a)
        assertEquals("ID(a)", tok1.toString())

        var cur = ts2
        var last: Token = tok1
        while (true) {
            val step = cur.next()
            when (step) {
                is Failure -> fail("No se esperaba error: ${step.error.humanReadable()}")
                is Success -> {
                    last = step.value.first
                    cur = step.value.second
                    if (last is EofToken) break
                }
            }
        }
        assertTrue(cur.isEof())
        assertEquals("EOF", last.toString())
    }

    @Test
    fun failure_propagates_from_tokenizer() {
        // El '$' es inválido → el tokenizer fallará cuando llegue ahí
        val ts = stream("let x = 5 $;")

        // Peek grande fuerza a producir tokens hasta la falla
        val r = ts.peek(1000)
        val err = assertFailure(r) // debe fallar
        // No “matcheamos” exacto el tipo, porque depende de tu error concreto;
        // alcanza con saber que es Failure y tiene span/mensaje.
        assertTrue(err.message.isNotBlank())
    }

    @Test
    fun peek_after_eof_returns_eof() {
        val ts0 = stream("a;")
        var cur = ts0
        while (true) {
            val step = cur.next()
            when (step) {
                is Failure -> fail("No se esperaba error: ${step.error.humanReadable()}")
                is Success -> {
                    val (tok, nxt) = step.value
                    cur = nxt
                    if (tok is EofToken) break
                }
            }
        }
        assertTrue(cur.isEof())

        // Peek(0) (y mayores) deben devolver EOF
        val p0 = assertSuccess(cur.peek(0))
        assertEquals("EOF", p0.toString())

        val p5 = assertSuccess(cur.peek(5))
        assertEquals("EOF", p5.toString())
    }

    @Test
    fun two_cursors_light_divergence() {
        val ts0 = stream("x=1;")

        val (t0, ts1) = assertSuccess(ts0.next())
        assertEquals("ID(x)", t0.toString())

        // ts0 sigue válido (no lo usamos normalmente, pero debería poder peekeear)
        val p0OnTs0 = assertSuccess(ts0.peek(0))
        assertEquals("ID(x)", p0OnTs0.toString())

        // ts1 ya ve el siguiente
        val p0OnTs1 = assertSuccess(ts1.peek(0))
        assertEquals("OP(ASSIGN)", p0OnTs1.toString())
    }

    @Test
    fun equivalence_with_tokencollector() {
        val src = """
            let name: string = "Joe";
            println(name);
        """.trimIndent()

        val tz = tokenizerV0(src)
        val byCollector = assertSuccess(TokenCollector.collectAll(tz)).map { it.toString() }

        val ts = BufferedStreamingTokenStream.of(tokenizerV0(src))
        val byStream = mutableListOf<String>()
        var cur: TokenStream = ts
        while (true) {
            when (val step = cur.next()) {
                is Failure -> fail("No se esperaba error: ${step.error.humanReadable()}")
                is Success -> {
                    val (tok, nxt) = step.value
                    byStream += tok.toString()
                    cur = nxt
                    if (tok is EofToken) break
                }
            }
        }

        assertEquals(byCollector, byStream)
    }
}
