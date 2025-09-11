package org.printscript.analyzer

import org.printscript.common.Version

object StreamingAnalyzerFactory {
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
                ReadInputSimpleArgRule(), // 1.1
            ),
        )
    }
}
