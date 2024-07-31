import kotlin.native.concurrent.AtomicInt
import kotlinx.cinterop.staticCFunction
import platform.posix.*

private val terminationRequested = AtomicInt(0)

private fun handleTerminationSignal(signal: Int) {
  terminationRequested.value = 1
}

fun suspendUntilTerminated() {
  signal(SIGINT, staticCFunction(::handleTerminationSignal))
  signal(SIGTERM, staticCFunction(::handleTerminationSignal))

  while (terminationRequested.value == 0) {
    sleep(1)
  }
}
