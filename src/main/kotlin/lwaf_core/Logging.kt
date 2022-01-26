@file:Suppress("unused")

package lwaf_core

object Logging
private val enabled = HashSet<String>()

fun Logging.enable(logType: String = "") {
    enabled.add(logType)
}

fun Logging.disable(logType: String) {
    enabled.remove(logType)
    enabled.removeAll { it.startsWith("$logType.") }
}

fun Logging.log(logType: String = "info", log: () -> String) {
    if (!enabled.any { logType.startsWith("$it.") || it == logType || it == "" }) return
    println(log())
}

fun Logging.error(log: () -> String) {
    println("ERROR: ${log()}")
}
