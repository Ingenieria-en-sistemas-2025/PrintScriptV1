package org.printscript.analyzer

import com.fasterxml.jackson.core.JsonProcessingException
import org.printscript.common.LabeledError
import org.printscript.common.Position
import org.printscript.common.Span

internal fun JsonProcessingException.toLabeledError(prefix: String): LabeledError {
    val location = this.location
    val span = if (location != null) {
        val line = (location.lineNr).coerceAtLeast(1)
        val col = (location.columnNr).coerceAtLeast(1)
        Span(Position(line, col), Position(line, col))
    } else {
        Span(Position(1, 1), Position(1, 1))
    }
    return LabeledError.of(span, "$prefix: ${this.originalMessage}")
}
