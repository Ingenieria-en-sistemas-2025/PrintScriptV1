package org.printscript.parser.factories

import org.printscript.parser.Parser

object GlobalParserFactory {
    fun forVersion(version: String): Parser? {
        return when (version) {
            "1.0" -> ParserFactoryV10.create()
            "1.1" -> ParserFactoryV11.create()
            else -> null
        }
    }
}
