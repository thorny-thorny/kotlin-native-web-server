import kotlinx.cinterop.*
import platform.posix.*

actual class Mutex {
  private val mutex: CPointer<pthread_mutex_t>?

  actual constructor() {
    // Memory leak
    mutex = platform.posix.malloc(sizeOf<pthread_mutex_t>().toULong()) as CPointer<pthread_mutex_t>? ?: throw Error("Failed to allocate memory for mutex")
    pthread_mutex_init(mutex, null)
  }

  actual fun lock() {
    pthread_mutex_lock(mutex)
  }

  actual fun unlock() {
    pthread_mutex_unlock(mutex)
  }
}
