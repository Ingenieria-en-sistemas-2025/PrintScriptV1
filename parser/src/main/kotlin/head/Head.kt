package head

import Keyword

sealed interface Head

data object Assign : Head

data class Kw(val kw: Keyword) : Head

data object Unknown : Head
