// Might try to use Mutex from https://github.com/Kotlin/kotlinx.atomicfu
expect class Mutex {
  constructor()
  fun lock()
  fun unlock()
}
