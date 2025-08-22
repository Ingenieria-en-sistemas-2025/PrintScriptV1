import config.FormatterConfig
import rules.DefaultSpacingRule

class Formatter(
    private val config: FormatterConfig,
    private val registry: RuleRegistry = DefaultRuleRegistry(config)
) {
    private val defaultSpacing = DefaultSpacingRule(config)

    fun format(tokens: List<Token>): String {
        val output = StringBuilder()
        fun emit(text: String) {
            if (text == " ") {
                if (output.isNotEmpty() && output.last() != ' ' && output.last() != '\n') output.append(' ')
            } else output.append(text)
        }

        for (i in tokens.indices) {
            val prev = tokens.getOrNull(i - 1)
            val current = tokens[i]
            val next = tokens.getOrNull(i + 1)

            if (current is EofToken) break

            val formatted = registry.findApplicableRule(prev, current, next)
            if (formatted != null) {
                emit(formatted)
                continue
            }

            defaultSpacing.apply(prev, current, next)?.let(::emit)
            emit(current.codeText)
        }
        return output.toString()
    }
}


