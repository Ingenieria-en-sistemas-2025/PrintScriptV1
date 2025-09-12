package org.printscript.analyzer

import org.printscript.analyzer.rules.IdentifierStyleRuleStreaming
import org.printscript.analyzer.rules.PrintlnSimpleArgRuleStreaming
import org.printscript.analyzer.rules.ReadInputSimpleArgRuleStreaming
import org.printscript.common.Version

object AnalyzerFactory {
    fun forVersion(v: Version): StreamingAnalyzer = when (v) {
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
