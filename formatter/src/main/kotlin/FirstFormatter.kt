import config.FormatterOptions

class FirstFormatter(
    private val config: FormatterOptions,
    private val registry: RuleRegistry = EnhancedRuleRegistry(config),
) : Formatter {

    override fun format(ts: TokenStream): Result<String, LabeledError> {
        val out = StringBuilder()

        fun emit(text: String) {
            if (text == " ") {
                if (out.isNotEmpty() && out.last() != ' ' && out.last() != '\n') out.append(' ')
            } else {
                out.append(text)
            }
        }

        fun step(stream: TokenStream, prev: Token?): Result<String, LabeledError> =
            stream.peek().flatMap { current ->
                if (current is EofToken) {
                    // ultima chance a registry de emitir prefijos (ej \n cuando prev es ;)
                    registry.findApplicableRule(prev, current, null)?.let(::emit)
                    Success(out.toString())
                } else {
                    stream.peek(1).flatMap { lookahead ->
                        val next = if (lookahead is EofToken) null else lookahead
                        // 1) prefijo (solo espacios/saltos)
                        registry.findApplicableRule(prev, current, next)?.let(::emit)
                        // 2) token
                        emit(current.codeText)
                        // 3) avanzar
                        stream.next().flatMap { (_, nextStream) -> step(nextStream, current) }
                    }
                }
            }

        return step(ts, null)
    }
}
