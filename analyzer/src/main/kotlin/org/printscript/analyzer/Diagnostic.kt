package org.printscript.analyzer

import org.printscript.common.Span

data class Diagnostic(val ruleId: String, val message: String, val span: Span, val severity: Severity = Severity.WARNING)
