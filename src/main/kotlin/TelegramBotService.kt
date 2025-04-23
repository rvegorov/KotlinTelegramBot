package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    val botToken: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().build()
    private fun makeRequest(method: String, queryString: String = ""): String {
        var url = "$TELEGRAM_API_URL/bot$botToken/$method"
        if (queryString != "") url += "?$queryString"
        val request: HttpRequest = HttpRequest.newBuilder(URI.create(url)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun getUpdates(updateId: Int): String {
        return makeRequest("getUpdates", "offset=$updateId")
    }

    fun sendMessage(chatId: Int, text: String): String {
        return makeRequest("sendMessage", "chat_id=$chatId&text=$text")
    }
}