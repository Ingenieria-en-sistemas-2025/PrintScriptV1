import org.printscript.ast.Assignment
import org.printscript.ast.Binary
import org.printscript.ast.Expression
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.ReadEnv
import org.printscript.ast.ReadInput
import org.printscript.ast.Statement
import org.printscript.ast.StatementStream
import org.printscript.ast.Step
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable
import org.printscript.common.Operator
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Type
import org.printscript.common.Version
import org.printscript.interpreter.GlobalInterpreterFactory
import org.printscript.interpreter.InputProvider
import org.printscript.interpreter.RunResult
import org.printscript.interpreter.errors.InterpreterError

fun pos(l: Int, c: Int) = Position(l, c)
fun span(l1: Int, c1: Int, l2: Int, c2: Int) = Span(pos(l1, c1), pos(l2, c2))

fun litNum(raw: String, l: Int, c: Int) =
    LiteralNumber(raw, span(l, c, l, c + raw.length))

fun litStr(v: String, l: Int, c: Int) =
    LiteralString(v, span(l, c, l, c + v.length + 2))

fun variable(name: String, l: Int, c: Int) =
    Variable(name, span(l, c, l, c + name.length))

fun binary(
    left: Expression,
    op: Operator,
    right: Expression,
    l1: Int,
    c1: Int,
    l2: Int,
    c2: Int,
) = Binary(left, right, op, span(l1, c1, l2, c2))

fun printlnNode(expr: Expression, l1: Int, c1: Int, l2: Int, c2: Int) =
    Println(expr, span(l1, c1, l2, c2))

fun varDecl(name: String, type: Type, init: Expression?, l1: Int, c1: Int, l2: Int, c2: Int) =
    VarDeclaration(name, type, init, span(l1, c1, l2, c2))

fun assignment(name: String, value: Expression, l1: Int, c1: Int, l2: Int, c2: Int) =
    Assignment(name, value, span(l1, c1, l2, c2))

fun readInputExpr(prompt: Expression, l1: Int, c1: Int, l2: Int, c2: Int) =
    ReadInput(prompt, span(l1, c1, l2, c2))

fun streamOf(vararg stmts: Statement): StatementStream {
    data class S(val rest: List<Statement>) : StatementStream {
        override fun nextStep(): Step = when {
            rest.isEmpty() -> Step.Eof
            else -> Step.Item(rest.first(), S(rest.drop(1)))
        }
    }
    return S(stmts.toList())
}

fun readEnvExpr(varNameExpr: Expression, l1: Int, c1: Int, l2: Int, c2: Int) =
    ReadEnv(varNameExpr, span(l1, c1, l2, c2))

fun run(
    stream: StatementStream,
    version: Version = Version.V1,
    input: InputProvider? = null,
    printer: ((String) -> Unit)? = null,
    collectAlso: Boolean = false,
): Result<RunResult, InterpreterError> {
    val interp = GlobalInterpreterFactory.forVersion(
        version = version,
        inputOverride = input,
        printer = printer,
        collectAlsoWithPrinter = collectAlso,
    )
    return interp.run(stream)
}
