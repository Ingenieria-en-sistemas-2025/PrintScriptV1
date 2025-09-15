package org.printscript.token

import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.token.dsl.TokenBuilder

object TestUtils {

    fun tokens(init: TokenBuilder.() -> TokenBuilder): TokenStream {
        return TokenBuilder()
            .let(init)
            .build()
    }

    fun <T> assertSuccess(result: Result<T, LabeledError>): T =
        when (result) {
            is Success -> result.value
            is Failure -> error("Expected Success, got Failure: ${result.error.humanReadable()}")
        }

    fun <T> assertFailure(result: Result<T, LabeledError>): LabeledError =
        when (result) {
            is Success -> error("Expected Failure, got Success: ${result.value}")
            is Failure -> result.error
        }
}
