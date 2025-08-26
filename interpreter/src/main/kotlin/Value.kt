sealed interface Value {
    data class Num(val n: Double) : Value
    data class Str(val s: String) : Value
}
