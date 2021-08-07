package me.thorny.webserver

typealias RequestHandler<CtxType> = (CtxType, Request) -> Response

abstract class WebServer<CtxType: Any?>(
  protected val handlerContext: CtxType,
  protected val handler: RequestHandler<CtxType>,
) {
  private var isRunning = false

  fun start(port: UShort) {
    if (isRunning) {
      throw Error("Can't start running server")
    }

    startSafe(port)
    isRunning = true
  }

  fun stop() {
    if (!isRunning) {
      throw Error("Can't stop stopped server")
    }

    stopSafe()
    isRunning = false
  }

  protected abstract fun stopSafe()
  protected abstract fun startSafe(port: UShort)
}
