package org.printscript.token

import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.token.dsl.TokenBuilder

object TestUtils {

    fun tokens(init: TokenBuilder.() -> TokenBuilder): TokenStream {
        // crear un nuevo TokenBuilder y pasarlo al bloque DSL
        return TokenBuilder().let {
            // ejecutar la lambda DSL sobre el builder (devuelve otro builder)
            init(it)
                // construir el org.printscript.token.TokenStream final a partir del builder
                .build()
        }
    }

    fun <T> assertSuccess(result: Result<T, LabeledError>): T =
        result.fold(
            onSuccess = { it },
            onFailure = { error("Expected Success, got Failure: ${it.humanReadable()}") },
        )

    fun <T> assertFailure(result: Result<T, LabeledError>): LabeledError =
        when (result) {
            is Success -> error("Expected Failure, got Success: ${result.value}")
            is Failure -> result.error
        }
}
