@file:Suppress("LongParameterList")

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

fun program(vararg s: Statement) = ProgramNode(s.toList())
