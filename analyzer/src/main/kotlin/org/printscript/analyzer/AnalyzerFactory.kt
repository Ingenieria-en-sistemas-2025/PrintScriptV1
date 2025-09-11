package org.printscript.analyzer

import org.printscript.common.Version

object AnalyzerFactory {
    fun forVersion(v: Version): Analyzer = when (v) {
        Version.V0 -> DefaultAnalyzer(
            listOf(
                IdentifierStyleRuleOld(),
                PrintlnSimpleArgRuleOld(),
            ),
        )
        Version.V1 -> DefaultAnalyzer(
            listOf(
                IdentifierStyleRuleOld(),
                PrintlnSimpleArgRuleOld(),
                ReadInputSimpleArgRuleOld(),
            ),
        )
    }
}
