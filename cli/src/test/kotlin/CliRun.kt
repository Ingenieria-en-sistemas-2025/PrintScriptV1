import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
import org.junit.jupiter.api.io.TempDir
import org.printscript.cli.PrintScriptCli
import org.printscript.cli.commands.AnalyzeCmd
import org.printscript.cli.commands.ExecuteCmd
import org.printscript.cli.commands.FormatCmd
import org.printscript.cli.commands.ValidateCmd
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

data class CliRun(val code: Int, val out: String, val err: String)

open class CliHarness {
    @TempDir
    lateinit var tmp: Path

    private lateinit var origOut: PrintStream
    private lateinit var origErr: PrintStream
    private lateinit var origIn: InputStream
    private lateinit var outBuf: ByteArrayOutputStream
    private lateinit var errBuf: ByteArrayOutputStream

    @BeforeTest
    fun setup() {
        origOut = System.out
        origErr = System.err
        origIn = System.`in`
        outBuf = ByteArrayOutputStream()
        errBuf = ByteArrayOutputStream()
        System.setOut(PrintStream(outBuf, true, Charsets.UTF_8))
        System.setErr(PrintStream(errBuf, true, Charsets.UTF_8))
    }

    @AfterTest
    fun teardown() {
        System.setOut(origOut)
        System.setErr(origErr)
        System.setIn(origIn)
    }

    fun file(name: String, content: String): Path {
        val p = tmp.resolve(name)
        Files.createDirectories(p.parent)
        Files.writeString(p, content, StandardCharsets.UTF_8)
        return p
    }

    fun json(name: String, content: String) = file(name, content)
    fun yaml(name: String, content: String) = file(name, content)

    fun run(vararg args: String, stdin: String? = null): CliRun {
        if (stdin != null) {
            System.setIn(ByteArrayInputStream(stdin.toByteArray(StandardCharsets.UTF_8)))
        }
        var code = 0
        try {
            // invocamos el CLI “real”
            PrintScriptCli()
                .subcommands(
                    ValidateCmd(),
                    ExecuteCmd(),
                    FormatCmd(),
                    AnalyzeCmd(),
                )
                .main(args.toList())
        } catch (pr: ProgramResult) {
            code = pr.statusCode
        }
        return CliRun(
            code = code,
            out = outBuf.toString(Charsets.UTF_8),
            err = errBuf.toString(Charsets.UTF_8),
        )
    }
}
