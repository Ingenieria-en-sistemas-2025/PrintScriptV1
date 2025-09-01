interface Analyzer {
    fun analize(program: ProgramNode, config: AnalyzerConfig): Result<DiagnosticReport, LabeledError>
}
