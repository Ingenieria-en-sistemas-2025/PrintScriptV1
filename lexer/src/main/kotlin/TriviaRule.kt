// Reglas que consumen caracteres sin emitir tokens
interface TriviaRule {

    /** Si matchea, devuelve el largo a saltar (>=1).
     *  Si no matchea, devuelve Success(0).
     *  Si detecta un problema, Failure(error).
     */

    fun matchLen(scanner: Scanner): Result<Int, LexerError>
}