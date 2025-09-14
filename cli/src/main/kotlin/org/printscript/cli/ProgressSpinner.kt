// package org.printscript.cli
//
// import kotlin.concurrent.thread
//
// class ProgressSpinner(private val label: String = "Parsing") {
//    @Volatile private var running = false
//    private var t: Thread? = null
//
//    fun start() {
//        if (running) return
//        running = true
//        t = thread(isDaemon = true) {
//            val frames = charArrayOf('|', '/', '-', '\\')
//            var i = 0
//            while (running) {
//                print("\r$label... ${frames[i % frames.size]}")
//                i++
//                Thread.sleep(90)
//            }
//            print("\r$label... done     \n")
//        }
//    }
//
//    fun stop() {
//        running = false
//        t?.join(250)
//    }
// }
