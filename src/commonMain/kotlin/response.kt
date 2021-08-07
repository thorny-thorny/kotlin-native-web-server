package me.thorny.webserver

class Response(val status: UShort, val contentType: String?, val body: String?, val headers: Map<String, String>?)
