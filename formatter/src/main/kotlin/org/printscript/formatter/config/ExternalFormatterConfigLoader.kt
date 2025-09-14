package org.printscript.formatter.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Traduce el JSON del TCK → FormatterOptions internas.
 */
object ExternalFormatterConfigLoader {
    const val indent = 2
    private val mapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val alias = mapOf(
        "enforce-spacing-around-equals" to "spaceAroundAssignment",
        "enforce-no-spacing-around-equals" to "noSpaceAroundAssignment",
        "enforce-spacing-after-colon-in-declaration" to "spaceAfterColonInDecl",
        "enforce-spacing-before-colon-in-declaration" to "spaceBeforeColonInDecl",
        "indent_size" to "indentSpaces",
        "indent-spaces" to "indentSpaces",
        "indent-inside-if" to "indentSpaces",
        "tabsize" to "indentSpaces",
        "spaceBeforeColonInDecl" to "spaceBeforeColonInDecl",
        "spaceAfterColonInDecl" to "spaceAfterColonInDecl",
        "line-breaks-after-println" to "blankLinesAfterPrintln", // Actualizado
        "line_breaks_after_println" to "blankLinesAfterPrintln", // Actualizado
        "mandatory-single-space-separation" to "mandatorySingleSpaceSeparation",
        "if-brace-below-line" to "ifBraceBelowLine",
        "if-brace-same-line" to "ifBraceSameLine",
    )

    fun load(configJsonUtf8: ByteArray?): FormatterOptions {
        if (configJsonUtf8 == null || configJsonUtf8.isEmpty()) return FormatterConfig()

        @Suppress("UNCHECKED_CAST")
        val raw = runCatching {
            mapper.readValue(configJsonUtf8, Map::class.java) as Map<String, Any?>
        }.getOrElse { return FormatterConfig() }

        // normaliza claves por alias
        val norm = mutableMapOf<String, Any?>()
        for ((k, v) in raw) norm[alias[k] ?: k] = v

        // '=' con banderas opuestas
        val eqYes = norm["spaceAroundAssignment"] as? Boolean
        val eqNo = norm["noSpaceAroundAssignment"] as? Boolean
        val spaceAroundAssign = when {
            eqYes != null -> eqYes
            eqNo != null -> !eqNo
            else -> true
        }

        val indent = (norm["indentSpaces"] as? Number)?.toInt()
            ?: (norm["indentSpaces"] as? String)?.toIntOrNull()
            ?: indent

        // Cambio clave: ahora busca "blankLinesAfterPrintln"
        val blank = (norm["blankLinesAfterPrintln"] as? Number)?.toInt() ?: 0
        val mandatorySpacing = (norm["mandatorySingleSpaceSeparation"] as? Boolean) ?: false

        val spaceBeforeColon = (norm["spaceBeforeColonInDecl"] as? Boolean) ?: false
        val spaceAfterColon = (norm["spaceAfterColonInDecl"] as? Boolean) ?: true

        val ifBraceBelowLine = (norm["ifBraceBelowLine"] as? Boolean) ?: false
        val ifBraceSameLine = (norm["ifBraceSameLine"] as? Boolean) ?: true

        return FormatterConfig(
            spaceBeforeColonInDecl = spaceBeforeColon,
            spaceAfterColonInDecl = spaceAfterColon,
            spaceAroundAssignment = spaceAroundAssign,
            blankLinesAfterPrintln = blank,
            indentSpaces = indent,
            mandatorySingleSpaceSeparation = mandatorySpacing,
            ifBraceBelowLine = ifBraceBelowLine,
            ifBraceSameLine = ifBraceSameLine,
        )
    }
}
