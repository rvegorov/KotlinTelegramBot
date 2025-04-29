package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(
    val botToken: String,
) {
    private val telegramApiUrl = "https://api.telegram.org"
    private val client: HttpClient = HttpClient.newBuilder().build()
    private fun makeRequest(request: HttpRequest): String{
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun makeGetRequest(method: String, queryString: String = ""): String {
        var url = "$telegramApiUrl/bot$botToken/$method"
        if (queryString != "") url += "?$queryString"
        val request: HttpRequest = HttpRequest.newBuilder(URI.create(url)).build()
        return makeRequest(request)
    }

    private fun makePostRequest(method: String, dataType: String, data: String): String {
        val url = "$telegramApiUrl/bot$botToken/$method"
        val request: HttpRequest = HttpRequest.newBuilder(URI.create(url))
            .header("Content-type", dataType)
            .POST(HttpRequest.BodyPublishers.ofString(data))
            .build()
        return makeRequest(request)
    }

    fun getUpdates(updateId: Int): String {
        return makeGetRequest("getUpdates", "offset=$updateId")
    }

    fun sendMessage(chatId: Int, text: String): String {
        val encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8)
        return makeGetRequest("sendMessage", "chat_id=$chatId&text=$encodedText")
    }

    fun sendMenu(chatId: Int): String {
        val menuBody = """
            {
                "chat_id": "$chatId",
                "text": "Меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучать слова",
                                "callback_data": "learnWords_clicked"
                            }
                        ],
                        [
                            {
                                "text": "Статистика",
                                "callback_data": "statistics_clicked"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        return makePostRequest("sendMessage", "application/json", menuBody)
    }
}
