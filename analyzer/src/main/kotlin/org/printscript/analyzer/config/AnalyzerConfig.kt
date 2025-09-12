package org.printscript.analyzer.config

data class AnalyzerConfig(val identifiers: IdentifiersConfig = IdentifiersConfig(), val printlnRule: PrintlnRuleConfig = PrintlnRuleConfig(), val readInputRule: ReadInputRuleConfig = ReadInputRuleConfig())
