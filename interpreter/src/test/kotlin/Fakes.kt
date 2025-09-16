import org.printscript.interpreter.InputProvider

class ListInputProvider(private val inputs: List<String>) : InputProvider {
    private var i = 0
    override fun read(prompt: String): String {
        val v = inputs.getOrNull(i) ?: ""
        i += 1
        return v
    }
}

class PrinterProbe : (String) -> Unit {
    val lines = mutableListOf<String>()
    override fun invoke(p: String) {
        lines += p
    }
}
