package org.printscript.formatter.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

object ExternalFormatterConfigLoader {
    const val indentDefault = 2
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
        "line-breaks-after-println" to "blankLinesAfterPrintln",
        "line_breaks_after_println" to "blankLinesAfterPrintln",
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

        val norm = mutableMapOf<String, Any?>()
        for ((k, v) in raw) norm[alias[k] ?: k] = v

        val eqYes = norm["spaceAroundAssignment"] as? Boolean
        val eqNo = norm["noSpaceAroundAssignment"] as? Boolean
        val spaceAroundAssign: Boolean? = when {
            eqYes != null -> eqYes
            eqNo != null -> !eqNo
            else -> null
        }

        val indent = (norm["indentSpaces"] as? Number)?.toInt()
            ?: (norm["indentSpaces"] as? String)?.toIntOrNull()
            ?: indentDefault

        val blank: Int? = (norm["blankLinesAfterPrintln"] as? Number)?.toInt()

        val mandatorySpacing: Boolean = (norm["mandatorySingleSpaceSeparation"] as? Boolean) ?: false
        val spaceBeforeColon: Boolean? = norm["spaceBeforeColonInDecl"] as? Boolean
        val spaceAfterColon: Boolean? = norm["spaceAfterColonInDecl"] as? Boolean
        val ifBraceBelowLine: Boolean = (norm["ifBraceBelowLine"] as? Boolean) ?: false
        val ifBraceSameLine: Boolean = (norm["ifBraceSameLine"] as? Boolean) ?: true

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
