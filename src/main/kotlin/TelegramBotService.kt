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

    private val client: HttpClient = HttpClient.newBuilder().build()

    private fun makeRequest(request: HttpRequest): String {
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun makeGetRequest(method: String, queryString: String = ""): String {
        var url = "$TELEGRAM_API_URL/bot$botToken/$method"
        if (queryString != "") url += "?$queryString"
        val request: HttpRequest = HttpRequest.newBuilder(URI.create(url)).build()
        return makeRequest(request)
    }

    private fun makePostRequest(method: String, dataType: String, data: String): String {
        val url = "$TELEGRAM_API_URL/bot$botToken/$method"
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
                "text": "<b>Меню</b>",
                "parse_mode" : "HTML",
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

    fun sendStatistics(chatId: Int, statistics: Statistics): String {
        val statisticsBody = """
            {
                "chat_id": "$chatId",
                "text": "<b>Статистика:</b>
                Выучено слов: ${statistics.learnedCount} из ${statistics.totalCount} (${statistics.percent}%)
                ",
                "parse_mode" : "HTML",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "назад",
                                "callback_data": "menu_clicked"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        return makePostRequest("sendMessage", "application/json", statisticsBody)
    }

    fun sendQuestion(chatId: Int, question: Question): String {
        val answerButtonsText = question.variants.mapIndexed { i, word ->
            """ [{
                                "text": "${word.translate}",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX+i}"
                            }]""".trimIndent()
        }.joinToString(", ")
        val questionBody = """
            {
                "chat_id": "$chatId",
                "text": "<b>${question.correctAnswer.original}:</b>
                ",
                "parse_mode" : "HTML",
                "reply_markup": {
                    "inline_keyboard": [
                            $answerButtonsText
                    ]
                }
            }
        """.trimIndent()
        return makePostRequest("sendMessage", "application/json", questionBody)
    }

    companion object {
        private const val TELEGRAM_API_URL = "https://api.telegram.org"
        const val TO_STATISTICS_DATA = "statistics_clicked"
        const val TO_LEARN_WORDS_DATA = "learnWords_clicked"
        const val TO_MENU_DATA = "menu_clicked"
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
        const val ANSWER_VARIANTS_COUNT = 4
        const val ALL_WORDS_LEARNED_TEXT = "Вы выучили все слова в базе"
    }
}
