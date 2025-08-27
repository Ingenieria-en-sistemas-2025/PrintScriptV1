interface Rule {
    val id: String
    fun check(program: ProgramNode, context: AnalyzerContext): List<Diagnostic>
}
