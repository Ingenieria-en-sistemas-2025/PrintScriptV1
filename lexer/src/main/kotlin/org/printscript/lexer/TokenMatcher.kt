package org.printscript.lexer

interface TokenMatcher {
    fun matchNext(scanner: Scanner): Match
}
