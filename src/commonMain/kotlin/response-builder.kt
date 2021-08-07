package me.thorny.webserver

class ResponseBuilder {
  var status: UShort = 202u
  var contentType: String? = null
  var body: String? = null
  private val headers = HashMap<String, String>()

  companion object {
    fun build(lambda: ResponseBuilder.() -> Unit): Response {
      val builder = ResponseBuilder()
      builder.lambda()
      return builder.build()
    }
  }

  fun setHeader(header: String, value: String) {
    headers[header] = value
  }

  private fun build(): Response {
    return Response(status, contentType, body, headers.toMap())
  }
}
