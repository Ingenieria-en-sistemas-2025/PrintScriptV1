import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.printscript.common.Keyword
import org.printscript.common.Position
import org.printscript.common.Separator
import org.printscript.common.Span
import org.printscript.formatter.config.FormatterOptions
import org.printscript.formatter.rules.BlankLinesBeforePrintlnRule
import org.printscript.formatter.rules.MandatorySpacingRule
import org.printscript.token.IdentifierToken
import org.printscript.token.KeywordToken
import org.printscript.token.SeparatorToken

class BlankLinesBeforePrintlnRuleTest {
    private val dummySpan: Span = Span(Position(1, 1), Position(1, 1))

    private class MockFormatterOptions(
        private val blankLinesAfterPrintlnValue: Int? = null,
        private val mandatorySingleSpaceSeparationValue: Boolean? = null,
    ) : FormatterOptions {
        override val spaceBeforeColonInDecl: Boolean? = null
        override val spaceAfterColonInDecl: Boolean? = null
        override val spaceAroundAssignment: Boolean? = null
        override val blankLinesAfterPrintln: Int? = blankLinesAfterPrintlnValue
        override val indentSpaces: Int = 2
        override val mandatorySingleSpaceSeparation: Boolean? = mandatorySingleSpaceSeparationValue
        override val ifBraceBelowLine: Boolean? = null
        override val ifBraceSameLine: Boolean? = null
    }

    @Test
    fun testblankLinesAfterPrintlnIsNull() {
        val config = MockFormatterOptions(blankLinesAfterPrintlnValue = null)
        val rule = BlankLinesBeforePrintlnRule(config)
        val printlnToken = KeywordToken(Keyword.PRINTLN, dummySpan)

        val result = rule.apply(null, printlnToken, null)
        assertNull(result)
    }

    @Test
    fun testBlankLinesAfterPrintlnIsZero() {
        val config = MockFormatterOptions(blankLinesAfterPrintlnValue = 0)
        val rule = BlankLinesBeforePrintlnRule(config)
        val printlnToken = KeywordToken(Keyword.PRINTLN, dummySpan)

        val result = rule.apply(null, printlnToken, null)
        assertNull(result)
    }

    @Test
    fun testBlankLinesAfterPrintlnIsFalse() {
        val config = MockFormatterOptions(blankLinesAfterPrintlnValue = -1)
        val rule = BlankLinesBeforePrintlnRule(config)
        val printlnToken = KeywordToken(Keyword.PRINTLN, dummySpan)

        val result = rule.apply(null, printlnToken, null)
        assertNull(result)
    }

    @Test
    fun testBlankLinesAfterPrintlnIsNegativeTwo() {
        val config = MockFormatterOptions(blankLinesAfterPrintlnValue = -2)
        val rule = BlankLinesBeforePrintlnRule(config)
        val printlnToken = KeywordToken(Keyword.PRINTLN, dummySpan)

        val result = rule.apply(null, printlnToken, null)

        assertNull(result)
    }

    @Test
    fun testBlankLinesAfterPrintlnIsOne() {
        val config = MockFormatterOptions(blankLinesAfterPrintlnValue = 1)
        val rule = BlankLinesBeforePrintlnRule(config)
        val printlnToken = KeywordToken(Keyword.PRINTLN, dummySpan)

        val result = rule.apply(null, printlnToken, null)
        assertEquals("\n\n", result)
    }

    @Test
    fun testMandatorySingleSpaceSeparation() {
        val config = MockFormatterOptions(mandatorySingleSpaceSeparationValue = true)
        val rule = MandatorySpacingRule(config)
        val constToken = KeywordToken(Keyword.CONST, dummySpan)
        val identifierToken = IdentifierToken("variable", dummySpan)

        val result = rule.apply(constToken, identifierToken, null)
        assertEquals(" ", result)
    }

    @Test
    fun testReturnSpaceAfterPrintln() {
        val config = MockFormatterOptions(mandatorySingleSpaceSeparationValue = true)
        val rule = MandatorySpacingRule(config)
        val printlnToken = KeywordToken(Keyword.PRINTLN, dummySpan)
        val lparenToken = SeparatorToken(Separator.LPAREN, dummySpan)

        val result = rule.apply(printlnToken, lparenToken, null)

        assertEquals(" ", result)
    }

    @Test
    fun testReturnSpaceAfterIf() {
        val config = MockFormatterOptions(mandatorySingleSpaceSeparationValue = true)
        val rule = MandatorySpacingRule(config)
        val ifToken = KeywordToken(Keyword.IF, dummySpan)
        val identifierToken = IdentifierToken("condition", dummySpan)

        val result = rule.apply(ifToken, identifierToken, null)

        assertNull(result)
    }
}
