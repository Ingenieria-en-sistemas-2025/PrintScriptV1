

import org.printscript.ast.Assignment
import org.printscript.ast.Binary
import org.printscript.ast.Expression
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.ReadInput
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable
import org.printscript.common.Operator
import org.printscript.common.Position
import org.printscript.common.Span
import org.printscript.common.Type

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
) =
    Binary(left, right, op, span(l1, c1, l2, c2))

fun printlnNode(expr: Expression, l1: Int, c1: Int, l2: Int, c2: Int) =
    Println(expr, span(l1, c1, l2, c2))

fun varDecl(name: String, type: Type, init: Expression?, l1: Int, c1: Int, l2: Int, c2: Int) =
    VarDeclaration(name, type, init, span(l1, c1, l2, c2))

fun assignment(name: String, value: Expression, l1: Int, c1: Int, l2: Int, c2: Int) =
    Assignment(name, value, span(l1, c1, l2, c2))

fun readInputExpr(prompt: Expression, l1: Int, c1: Int, l2: Int, c2: Int) =
    ReadInput(prompt, span(l1, c1, l2, c2))
