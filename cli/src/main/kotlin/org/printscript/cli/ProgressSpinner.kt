package org.printscript.cli

class ProgressSpinner(private val label: String) {
    companion object {
        private const val DEFAULT_INTERVAL_MS = 80L
        private val FRAMES = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    }

    @Volatile private var running = false // indica si el  spinner está activo
    private var thread: Thread? = null // referencia al hilo que está corriendo el bucle de animación.

    fun start() {
        if (running) return
        running = true
        thread = Thread { // Crea un hilo en segundo plano
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
//
