import org.printscript.common.Failure
import org.printscript.common.Success
import org.printscript.token.BooleanLiteralToken
import org.printscript.token.EofToken
import org.printscript.token.IdentifierToken
import org.printscript.token.KeywordToken
import org.printscript.token.NumberLiteralToken
import org.printscript.token.OperatorToken
import org.printscript.token.SeparatorToken
import org.printscript.token.StringLiteralToken
import org.printscript.token.TestUtils
import org.printscript.token.Token
import org.printscript.token.TokenStream
import org.printscript.token.TypeToken
import org.printscript.token.dsl.TokenBuilder

object Utilities {

    fun Token.toView(): TokenView = when (this) {
        is KeywordToken -> TokenView("KW", this.kind.name)
        is IdentifierToken -> TokenView("ID", this.identifier)
        is NumberLiteralToken -> TokenView("NUM", this.raw)
        is StringLiteralToken -> TokenView("STR", this.literal)
        is BooleanLiteralToken -> TokenView("BOOL", this.value.toString())
        is OperatorToken -> TokenView("OP", this.operator.name)
        is SeparatorToken -> TokenView("SEP", this.separator.name)
        is TypeToken -> TokenView("TYPE", this.type.name)
        is EofToken -> TokenView("EOF", null)
        else -> TokenView(this::class.simpleName ?: "UNK", null)
    }

    fun TokenStream.readAll(): List<Token> {
        val out = mutableListOf<Token>()
        var cur: TokenStream = this
        while (true) {
            when (val r = cur.next()) {
                is Success -> {
                    val (tok, nxt) = r.value
                    out += tok
                    cur = nxt
                    if (tok is EofToken) break
                }
                is Failure -> error("Fallo leyendo expectativa (TokenStream): ${r.error.message}")
            }
        }
        return out
    }

    fun buildExpected(init: TokenBuilder.() -> TokenBuilder): List<TokenView> =
        TestUtils.tokens(init).readAll().map { it.toView() }

    // Compara actual vs esperado en vistas
    fun assertLexEqualsWithBuilder(
        src: String,
        lexActual: (String) -> List<Token>,
        init: TokenBuilder.() -> TokenBuilder,
    ) {
        val actualViews = lexActual(src).map { it.toView() }
        val expectedViews = buildExpected(init)
        kotlin.test.assertEquals(expectedViews, actualViews)
    }
}
