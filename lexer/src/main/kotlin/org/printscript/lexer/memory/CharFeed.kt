package org.printscript.lexer.memory

interface CharFeed {
    fun ensureAvailable(absIndex: Int): Boolean
    fun charAt(absIndex: Int): Char?
    fun fixedSlice(startAbs: Int, len: Int): CharSequence
    fun rollingSlice(startAbs: Int): CharSequence
    fun eofFrom(absIndex: Int): Boolean
    fun pin(minAbsIndex: Int) {}
    fun unpin(minAbsIndex: Int) {}
}
