package org.printscript.lexer.config

import org.printscript.common.Version

object LexingConfigFactory {
    fun forVersion(v: Version): LexingConfig = when (v) {
        Version.V0 -> PrintScriptv0MapConfig().let { LexingConfig(it.rules(), it.triviaRules(), it.creators()) }
        Version.V1 -> PrintScriptv1MapConfig().let { LexingConfig(it.rules(), it.triviaRules(), it.creators()) }
    }
}
