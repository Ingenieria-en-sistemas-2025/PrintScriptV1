package org.printscript.formatter.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Traduce el JSON del TCK â†’ FormatterOptions internas.
 */
object ExternalFormatterConfigLoader {
    const val indent = 4
    private val mapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    // alias externos â†’ internos
    private val alias = mapOf(
        "enforce-spacing-around-equals" to "spaceAroundAssignment",
        "enforce-no-spacing-around-equals" to "noSpaceAroundAssignment",
        "enforce-spacing-after-colon-in-declaration" to "spaceAfterColonInDecl",
        "enforce-spacing-before-colon-in-declaration" to "spaceBeforeColonInDecl",
        "indent_size" to "indentSpaces",
        "indent-spaces" to "indentSpaces",
        "tabsize" to "indentSpaces",
        "spaceBeforeColonInDecl" to "spaceBeforeColonInDecl",
        "spaceAfterColonInDecl" to "spaceAfterColonInDecl",
        "line-breaks-after-println" to "blankLinesBeforePrintln",
        "line_breaks_after_println" to "blankLinesBeforePrintln",
        "mandatory-single-space-separation" to "mandatorySingleSpaceSeparation",
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

        val blank = (norm["blankLinesBeforePrintln"] as? Number)?.toInt() ?: 0
        val mandatorySpacing = (norm["mandatorySingleSpaceSeparation"] as? Boolean) ?: false

        val spaceBeforeColon = (norm["spaceBeforeColonInDecl"] as? Boolean) ?: false
        val spaceAfterColon = (norm["spaceAfterColonInDecl"] as? Boolean) ?: true

        return FormatterConfig(
            spaceBeforeColonInDecl = spaceBeforeColon,
            spaceAfterColonInDecl = spaceAfterColon,
            spaceAroundAssignment = spaceAroundAssign,
            blankLinesBeforePrintln = blank,
            indentSpaces = indent,
            mandatorySingleSpaceSeparation = mandatorySpacing,
        )
    }
}
