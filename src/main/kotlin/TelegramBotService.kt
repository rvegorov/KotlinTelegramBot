package org.example

import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val botToken: String,
    private val json: Json
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
        val response = makeRequest(request)
        return response
    }

    fun getUpdates(updateId: Long): String {
        return makeGetRequest("getUpdates", "offset=$updateId")
    }

    private fun sendMessage(sendMessageRequest: SendMessageRequest): String {
        val messageBody = json.encodeToString(sendMessageRequest)
        return makePostRequest("sendMessage", "application/json", messageBody)
    }

    fun sendText(chatId: Long, text: String): String {
        val messageBody = SendMessageRequest(
            chatId = chatId.toString(),
            text = text,
        )
        return sendMessage(messageBody)
    }

    fun sendMenu(chatId: Long): String {
        val menuBody =
            SendMessageRequest(
                chatId = chatId.toString(),
                text = "Меню",
                replyMarkup = ReplyMarkup(
                    listOf(
                        listOf(Button("Изучать слова", TO_LEARN_WORDS_DATA)),
                        listOf(Button("Статистика", TO_STATISTICS_DATA)),
                    )
                )
            )
        return sendMessage(menuBody)
    }

    fun sendStatistics(chatId: Long, statistics: Statistics): String {
        val statisticsBody = SendMessageRequest(
            chatId = chatId.toString(),
            text = """Статистика:
                Выучено слов: ${statistics.learnedCount} из ${statistics.totalCount} (${statistics.percent}%)""",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        Button(
                            text = "назад",
                            callbackData = TO_MENU_DATA,
                        )
                    )
                )
            ),
        )
        return sendMessage(statisticsBody)
    }

    fun sendQuestion(chatId: Long, question: Question): String {
        val questionBody = SendMessageRequest(
            chatId = chatId.toString(),
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(question.variants.mapIndexed { i, word ->
                    Button(word.translate, CALLBACK_DATA_ANSWER_PREFIX + i)
                }, listOf(Button("назад", TO_MENU_DATA)))
            )
        )
        return sendMessage(questionBody)
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
