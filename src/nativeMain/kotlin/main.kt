import me.thorny.webserver.ResponseBuilder
import kotlin.native.concurrent.AtomicInt

fun main() {
  val port: UShort = 8888u
  val server = MicroHttpDWebServer(AtomicInt(0)) { ctx, request ->
    // This lambda runs on global thread, and it must not capture any local variable! https://kotlinlang.org/api/latest/jvm/stdlib/kotlinx.cinterop/static-c-function.html
    // You can use ctx argument tho (It better be atomic or might get frozen)
    println("Handling request ${request.method} ${request.url}")

    val isGet = request.method.uppercase() == "GET" // Ignore other methods for stats
    val isFaviconRequest = request.url.lowercase() == "/favicon.ico" // Ignore these for stats

    val numberOfRequests = if (isGet && !isFaviconRequest) ctx.addAndGet(1) else ctx.value

    ResponseBuilder.build {
      if (isGet && !isFaviconRequest && request.url != "/404") {
        status = 200u
        contentType = "text/html; charset=UTF-8"
        body = """
          <html>
            <body>
              <h3>Hello!</h3>
              <p>You've visited url <b>${request.url}</b></p>
              <p>Total number of requests made: <b>$numberOfRequests</b></p>
              <br />
              <p>Links:</p>
              <ul>
                <li><a href="https://www.gnu.org/software/libmicrohttpd/">libmicrohttpd</a></li>
                <li><a href="https://github.com/thorny-thorny/kotlin-native-web-server">Project page @ github</a></li>
              </ul>
            </body>
          </html>
        """.trimIndent()
      } else {
        status = 404u
      }
    }
  }

  server.start(port)
  println("Server is listening port $port")
  println("Press enter to stop")

  // You can start more servers on different ports here
  // It should work as long as they all are created/started/stopped on the same thread

  readLine()

  server.stop()
}
