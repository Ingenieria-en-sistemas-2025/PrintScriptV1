import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter

class CollectorEmitter : DiagnosticEmitter {
    val diags = mutableListOf<Diagnostic>()
    override fun report(diagnostic: Diagnostic) { diags += diagnostic }
}
