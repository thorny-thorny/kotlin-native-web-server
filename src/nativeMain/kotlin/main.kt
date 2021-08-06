import kotlinx.cinterop.*
import libmicrohttpd.*

val handleRequest: MHD_AccessHandlerCallback? = staticCFunction { cls, connection, url, method, version, upload_data, upload_data_size, con_cls ->
  // Handler runs in a not-main thread, so this is required otherwise memory allocation goes nuts https://youtrack.jetbrains.com/issue/KT-44283
  initRuntimeIfNeeded()

  // I guess it's required to handle String.cstr memory leak and other code just in case
  memScoped {
    println("Handling request ${method?.toKString() ?: "-"} ${url?.toKString() ?: "-"}")

    val body = "<html><body>Hello, browser!</body></html>"

    // MHD_RESPMEM_MUST_COPY works here. For MHD_RESPMEM_PERSISTENT it responds with random string from memory on the first request
    // Might be because body bytes got freed before the end of the lambda
    val response = MHD_create_response_from_buffer(body.length.toULong(), body.cstr, MHD_ResponseMemoryMode.MHD_RESPMEM_MUST_COPY)
    MHD_add_response_header(response, "Content-Type", "text/html; charset=UTF-8")

    val ret = MHD_queue_response(connection, MHD_HTTP_OK, response)

    MHD_destroy_response(response)

    ret
  }
}

fun main() {
  val port: UShort = 8888u
  val daemon = MHD_start_daemon(MHD_USE_INTERNAL_POLLING_THREAD, port, null, null, handleRequest, null, MHD_OPTION_END)
  if (daemon == null) {
    println("Failed to start daemon")
    return
  }

  println("Daemon started, listening port $port")
  println("Press enter to stop")

  readLine()

  MHD_stop_daemon(daemon)
}
