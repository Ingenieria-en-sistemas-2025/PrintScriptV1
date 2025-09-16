import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.printscript.common.Version
import org.printscript.runner.helpers.VersionMapper
import kotlin.test.assertFailsWith

class VersionMapperTest {

    @Test
    fun testParseReturnsV0ForNullInput() {
        assertEquals(Version.V0, VersionMapper.parse(null))
    }

    @Test
    fun testParseReturnsV0ForEmptyString() {
        assertEquals(Version.V0, VersionMapper.parse(""))
    }

    @Test
    fun testParseReturnsV0ForVersion10() {
        assertEquals(Version.V0, VersionMapper.parse("1.0"))
        assertEquals(Version.V0, VersionMapper.parse("v1.0"))
    }

    @Test
    fun testParseReturnsV1ForVersion11() {
        assertEquals(Version.V1, VersionMapper.parse("1.1"))
        assertEquals(Version.V1, VersionMapper.parse("v1.1"))
    }

    @Test
    fun testParseHandlesWhitespace() {
        assertEquals(Version.V0, VersionMapper.parse("  1.0  "))
        assertEquals(Version.V1, VersionMapper.parse("  v1.1  "))
    }

    @Test
    fun testParseThrowsErrorForUnknownVersion() {
        assertFailsWith<IllegalStateException> {
            VersionMapper.parse("2.0")
        }

        assertFailsWith<IllegalStateException> {
            VersionMapper.parse("unknown")
        }
    }
}
