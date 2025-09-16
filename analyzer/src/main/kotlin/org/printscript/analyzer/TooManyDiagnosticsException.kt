package org.printscript.analyzer

class TooManyDiagnosticsException(val maxErrors: Int, val total: Int) : RuntimeException()
