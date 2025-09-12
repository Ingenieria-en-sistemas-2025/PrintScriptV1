import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.analyzer.AstWalk
import org.printscript.ast.Binary
import org.printscript.ast.IfStmt
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.VarDeclaration
import org.printscript.common.Operator
import org.printscript.common.Type
import kotlin.test.Test

class AstWalkTest {

    @Test
    fun expressionsOf_VarDeclWithNullInit_isEmpty() {
        val s = VarDeclaration("x", Type.NUMBER, null, span(1, 1, 1, 10))
        val seq = AstWalk.expressionsOf(s).toList()
        assertTrue(seq.isEmpty())
    }

    @Test
    fun expressionsOf_VarDeclWithBinary_containsRootAndChildren() {
        val init = binary(litNum("1", 1, 20), Operator.PLUS, litNum("2", 1, 24), 1, 20, 1, 25)
        val s = VarDeclaration("x", Type.NUMBER, init, span(1, 1, 1, 26))

        val xs = AstWalk.expressionsOf(s).toList()
        assertEquals(3, xs.size)
        assertTrue(xs[0] is Binary)
        assertTrue(xs.any { it is LiteralNumber && it.raw == "1" })
        assertTrue(xs.any { it is LiteralNumber && it.raw == "2" })
    }

    @Test
    fun expressionsOf_Println_containsInnerExpression() {
        val s = printlnNode(litStr("hi", 1, 10), 1, 1, 1, 14)
        val xs = AstWalk.expressionsOf(s).toList()
        assertEquals(1, xs.size)
        assertTrue(xs[0] is LiteralString)
    }

    @Test
    fun expressionsOf_If_traversesConditionThenElse() {
        val cond = binary(litNum("1", 1, 5), Operator.PLUS, litNum("2", 1, 9), 1, 5, 1, 10)
        val thenStmt = printlnNode(litStr("A", 2, 10), 2, 1, 2, 13)
        val elseStmt = printlnNode(litStr("B", 3, 10), 3, 1, 3, 13)

        val ifs = IfStmt(cond, listOf(thenStmt), listOf(elseStmt), span(1, 1, 3, 13))
        val xs = AstWalk.expressionsOf(ifs).toList()

        assertTrue(xs.any { it is Binary })
        assertTrue(xs.count { it is LiteralNumber } == 2)
        assertTrue(xs.count { it is LiteralString } == 2)
    }
}
