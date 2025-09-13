package org.printscript.analyzer.loader

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.common.LabeledError
import org.printscript.common.Result
import java.io.InputStream

interface ConfigReader {
    fun load(input: InputStream): Result<AnalyzerConfig, LabeledError>
}
