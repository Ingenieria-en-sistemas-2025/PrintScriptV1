package org.printscript.analyzer

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.rules.IdentifierStyleRuleStreaming
import org.printscript.analyzer.rules.PrintlnSimpleArgRuleStreaming
import org.printscript.analyzer.rules.ReadInputSimpleArgRuleStreaming
import org.printscript.common.Version

object AnalyzerFactory {

    fun forVersion(v: Version, cfg: AnalyzerConfig): StreamingAnalyzer {
        val rules = buildList {
            if (cfg.identifiers.enabled) add(IdentifierStyleRuleStreaming())
            if (cfg.printlnRule.enabled) add(PrintlnSimpleArgRuleStreaming())
            if (v == Version.V1 && cfg.readInputRule.enabled) add(ReadInputSimpleArgRuleStreaming())
        }
        return DefaultStreamingAnalyzer(rules)
    }

    @Deprecated("Preferir forVersion(version, config)")
    fun forVersion(v: Version): StreamingAnalyzer =
        when (v) {
            Version.V0 -> DefaultStreamingAnalyzer(
                listOf(
                    IdentifierStyleRuleStreaming(),
                    PrintlnSimpleArgRuleStreaming(),
                ),
            )
            Version.V1 -> DefaultStreamingAnalyzer(
                listOf(
                    IdentifierStyleRuleStreaming(),
                    PrintlnSimpleArgRuleStreaming(),
                    ReadInputSimpleArgRuleStreaming(),
                ),
            )
        }
}
