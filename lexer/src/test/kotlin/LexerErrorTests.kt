import org.printscript.common.Position
import org.printscript.common.Span
import org.printscript.lexer.LongestMatchTokenMatcher
import org.printscript.lexer.TokenFactory
import org.printscript.lexer.Tokenizer
import org.printscript.lexer.error.LexerError
import org.printscript.lexer.error.UnexpectedChar
import org.printscript.lexer.error.UnterminatedCommentBlock
import org.printscript.lexer.error.UnterminatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class LexerErrorTests {

    // Helpers para comparar posiciones (línea/columna)
    private fun beforeOrEqual(a: Position, b: Position): Boolean =
        (a.line < b.line) || (a.line == b.line && a.column <= b.column)

    private fun strictlyBefore(a: Position, b: Position): Boolean =
        (a.line < b.line) || (a.line == b.line && a.column < b.column)

    // Helpers para spans
    private fun isConsistentSpan(span: Span): Boolean =
        beforeOrEqual(span.start, span.end) // permite longitud 0

    private fun isNonEmptySpan(span: Span): Boolean =
        strictlyBefore(span.start, span.end) // requiere longitud > 0

    // --- Helpers (reutilizo los tuyos del otro test) ---
    private fun createTokenizer(src: String): Tokenizer {
        val cfg = org.printscript.lexer.config.PrintScriptv1MapConfig()
        val matcher = LongestMatchTokenMatcher(cfg.rules())
        val skipper = org.printscript.lexer.trivia.CompositeTriviaSkipper(cfg.triviaRules())
        val factory = TokenFactory(cfg.creators())
        return Tokenizer.of(src, matcher, skipper, factory)
    }

    private fun lexError(src: String): LexerError {
        val tz = createTokenizer(src)
        return when (val r = TokenCollector.collectAll(tz)) {
            is org.printscript.common.Success -> fail("Se esperaba Failure, pero se obtuvo Success con ${r.value.size} tokens")
            is org.printscript.common.Failure -> r.error
        }
    }

    // --- Tests de errores ---

    @Test
    fun simboloDesconocido_devuelveUnexpectedChar_yMensajeCorrecto() {
        val err = lexError("let x = 5 $;")
        val e = assertIs<UnexpectedChar>(err)
        assertEquals("Símbolo inesperado: '\$'", e.message)
        assertTrue(isNonEmptySpan(e.span), "Span inválido para UnexpectedChar")
    }

    @Test
    fun comentarioDeBloqueSinCerrar_devuelveUnterminatedCommentBlock() {
        val err = lexError("/* comentario...")
        val e = assertIs<UnterminatedCommentBlock>(err)
        assertEquals("Comentario de bloque no cerrado", e.message)
        assertTrue(isConsistentSpan(e.span), "Span inválido para UnterminatedCommentBlock")
    }

    @Test
    fun stringSinCerrar_enEOF_devuelveUnterminatedString_yMensajeDefault() {
        val err = lexError("""let s: string = "hola""")
        val e = assertIs<UnterminatedString>(err)
        assertEquals("Cadena sin cerrar", e.message)
        assertTrue(isConsistentSpan(e.span), "Span inválido para UnterminatedString")
    }

    @Test
    fun stringSinCerrar_porNuevaLinea_devuelveUnterminatedString() {
        val err = lexError(
            """
            println("hola
            """.trimIndent(),
        )
        val e = assertIs<UnterminatedString>(err)
        assertEquals("Cadena sin cerrar", e.message)
    }

    @Test
    fun mezclaDeCodigoConStringSinCerrar_reportaUnterminatedString() {
        val err = lexError("""let a: string = "x; let b = 2;""")
        val e = assertIs<UnterminatedString>(err)
        assertEquals("Cadena sin cerrar", e.message)
    }
}
