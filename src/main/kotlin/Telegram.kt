package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]

    println(getRequestResponseString(botToken, "getMe"))
    println(getRequestResponseString(botToken, "getUpdates"))
}

fun getRequestResponseString(token: String, method: String): String {
    val urlGetMe = "https://api.telegram.org/bot$token/$method"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder(URI.create(urlGetMe)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body()
}
