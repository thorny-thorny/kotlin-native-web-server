import kotlinx.cinterop.*
import libmicrohttpd.*
import me.thorny.webserver.*
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze

// Not great not terrible, staticCFunction can access only global vars
// TODO: freeze() might be unnecessary here
// TODO: try to pass context and handler via cls arg of MHD_AccessHandlerCallback
val contextMap = AtomicReference(mapOf<String, Any>().freeze())
val handlerMap = AtomicReference(mapOf<String, RequestHandler<Any?>>().freeze())

@SharedImmutable
val mapsMutex = Mutex()

val handleRequest: MHD_AccessHandlerCallback? = staticCFunction { cls, connection, url, method, version, upload_data, upload_data_size, con_cls ->
  // Handler runs in a not-main thread, so this is required otherwise String.cstr crashes https://youtrack.jetbrains.com/issue/KT-44283
  initRuntimeIfNeeded()

  try {
    // I guess it's required to handle String.cstr memory leak and other code just in case
    // Also memScope silences internal throws so internal try-catch is required
    memScoped {
      try {
        val id = (cls as CPointer<ByteVar>?)?.toKString() ?: throw Error("No id passed")

        mapsMutex.lock()

        val handlerContext = contextMap.value[id]
        val handler = handlerMap.value[id]

        mapsMutex.unlock()

        if (handler == null || method == null || url == null) {
          throw Error("No essential data passed")
        }

        val request = Request(method.toKString(), url.toKString())
        val response = handler(handlerContext, request)

        val body = response.body ?: ""

        // MHD_RESPMEM_MUST_COPY works here. For MHD_RESPMEM_PERSISTENT it responds with random string from memory on the first request
        // Might be because body bytes got freed before the end of the lambda
        val mhdResponse = MHD_create_response_from_buffer(
          body.length.toULong(),
          body.cstr,
          MHD_ResponseMemoryMode.MHD_RESPMEM_MUST_COPY,
        )
        if (response.contentType != null) {
          MHD_add_response_header(mhdResponse, MHD_HTTP_HEADER_CONTENT_TYPE, response.contentType)
        }
        if (response.headers != null) {
          for ((header, value) in response.headers) {
            MHD_add_response_header(mhdResponse, header, value)
          }
        }

        val ret = MHD_queue_response(connection, response.status.toUInt(), mhdResponse)
        MHD_destroy_response(mhdResponse)
        ret
      } catch (_: Error) {
        MHD_NO
      }
    }
  } catch (_: Error) {
    MHD_NO
  }
}

class MicroHttpDWebServer<CtxType: Any?>(
  handlerContext: CtxType,
  handler: RequestHandler<CtxType>,
): WebServer<CtxType>(handlerContext, handler) {
  private var daemon: CPointer<cnames.structs.MHD_Daemon>? = null
  private val id = randomString(10)
  private var cid: CPointer<ByteVar>? = null
  private val arena = Arena()

  override fun startSafe(port: UShort) {
    cid = id.cstr.getPointer(arena)

    val daemon = MHD_start_daemon(MHD_USE_INTERNAL_POLLING_THREAD, port, null, null, handleRequest, cid, MHD_OPTION_END)
    if (daemon == null) {
      throw Error("Failed to start libmicrohttpd daemon on port $port")
    }

    mapsMutex.lock()

    if (handlerContext != null) {
      contextMap.value = contextMap.value.mutateFrozenCopy {
        this[id] = handlerContext
      }
    }

    handlerMap.value = handlerMap.value.mutateFrozenCopy {
      this[id] = handler as RequestHandler<Any?> /* = (kotlin.Any?, me.thorny.webserver.Request) -> me.thorny.webserver.Response */
    }

    mapsMutex.unlock()
  }

  override fun stopSafe() {
    cid = null
    arena.clear()

    mapsMutex.lock()

    contextMap.value = contextMap.value.mutateFrozenCopy {
      remove(id)
    }
    handlerMap.value = handlerMap.value.mutateFrozenCopy {
      remove(id)
    }

    mapsMutex.unlock()

    MHD_stop_daemon(daemon)
  }
}

private fun randomString(length: Int): String {
  val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
  return (1..length)
    .map { charset.random() }
    .joinToString("")
}

private fun <K, V> Map<K, V>.mutateFrozenCopy(mutate: MutableMap<K, V>.() -> Unit): Map<K, V> {
  val mutableMap = this.toMutableMap()
  mutableMap.mutate()
  return mutableMap.toMap().freeze()
}
