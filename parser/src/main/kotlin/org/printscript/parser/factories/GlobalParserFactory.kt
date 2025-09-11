package org.printscript.parser.factories

import org.printscript.common.Version
import org.printscript.parser.Parser

object GlobalParserFactory {
    fun forVersion(version: Version): Parser? {
        return when (version) {
            Version.V0 -> ParserFactoryV10.create()
            Version.V1 -> ParserFactoryV11.create()
        }
    }
}
