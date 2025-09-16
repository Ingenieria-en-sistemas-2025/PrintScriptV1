import org.junit.jupiter.api.Assertions.assertEquals
import org.printscript.analyzer.AstWalk
import org.printscript.ast.IfStmt
import org.printscript.common.Operator
import org.printscript.common.Position
import org.printscript.common.Span
import org.printscript.common.Type
import kotlin.test.Test

class AstWalkPreorderTest {
    @Test
    fun expressionsOf_recorridoPreorden_cubre_todas_las_ramas() {
        // if ( ("H" + "i") ) { println( (1 + 2) ) } else { let x: number = readInput("N"); }
        val cond = binary(litStr("H", 1, 6), Operator.PLUS, litStr("i", 1, 12), 1, 6, 1, 15)
        val thenPrint = printlnNode(binary(litNum("1", 1, 28), Operator.PLUS, litNum("2", 1, 32), 1, 28, 1, 33), 1, 20, 1, 35)
        val elseLetInit = readInputExpr(litStr("N", 1, 57), 1, 48, 1, 60)
        val elseLet = varDecl("x", Type.NUMBER, elseLetInit, 1, 40, 1, 61)

        val ifStmt = IfStmt(
            condition = cond,
            thenBranch = listOf(thenPrint),
            elseBranch = listOf(elseLet),
            Span(Position(1, 1), Position(2, 2)),
        )

        val exprs = AstWalk.expressionsOf(ifStmt).toList()

        // Preorden: visita el nodo y luego sus hijos (Binary: left, right), Grouping (si hubiera), ReadInput(prompt)
        val expectedPretty = listOf(
            // condition: ("H" + "i")
            "\"H\"",
            "\"i\"",
            // then: println(1 + 2)
            "1",
            "2",
            // else: readInput("N")
            "\"N\"",
        )

        val pretty = exprs.map {
            when (it) {
                is org.printscript.ast.LiteralString -> "\"${it.value}\""
                is org.printscript.ast.LiteralNumber -> it.raw
                is org.printscript.ast.Variable -> it.name
                else -> it::class.simpleName.orEmpty()
            }
        }

        assertEquals(expectedPretty.sorted(), pretty.filter { it == "\"H\"" || it == "\"i\"" || it == "1" || it == "2" || it == "\"N\"" }.sorted())
    }
}
