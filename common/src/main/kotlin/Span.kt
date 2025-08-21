data class Span(val start: Position, val end: Position) {
    companion object {
        fun merge(a: Span, b: Span) = Span(
            start = if (a.start.line < b.start.line || (a.start.line == b.start.line && a.start.column <= b.start.column)) a.start else b.start,
            end   = if (a.end.line > b.end.line   || (a.end.line   == b.end.line   && a.end.column   >= b.end.column))   a.end   else b.end
        )
    }
}
