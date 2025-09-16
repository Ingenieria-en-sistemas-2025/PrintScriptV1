package org.printscript.cli

import kotlin.concurrent.thread

class ProgressSpinner(private val label: String) {
    companion object {
        private const val DEFAULT_INTERVAL_MS = 80L
        private val FRAMES = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    }

    @Volatile private var running = false
    private var thread: Thread? = null

    fun start() {
        if (running) return
        running = true
        thread = Thread {
            var i = 0
            while (running) {
                print("\r$label ${FRAMES[i % FRAMES.size]}")
                i++
                try {
                    Thread.sleep(DEFAULT_INTERVAL_MS)
                } catch (_: InterruptedException) { /* ignore */ }
            }
            print("\r") // limpiar línea
        }.also {
            it.isDaemon = true
            it.start()
        }
    }

    fun stop() {
        running = false
        thread?.interrupt()
        thread = null
    }
}
