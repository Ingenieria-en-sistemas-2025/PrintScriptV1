package org.printscript.analyzer

import org.printscript.common.Version

object AnalyzerFactory {
    fun forVersion(v: Version): Analyzer = when (v) {
        Version.V0 -> DefaultAnalyzer(
            listOf(
                IdentifierStyleRule(),
                PrintlnSimpleArgRule(),
            ),
        )
        Version.V1 -> DefaultAnalyzer(
            listOf(
                IdentifierStyleRule(),
                PrintlnSimpleArgRule(),
                ReadInputSimpleArgRule(),
            ),
        )
    }
}
