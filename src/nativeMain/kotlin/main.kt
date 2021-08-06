fun main() {
  println("What's your name?")
  val name = readLine() ?: return
  println("Nice to meet you, $name!")
}
