package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    val chat: Chat?,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    val message: Message?,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: String,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)


@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<Button>>
)


@Serializable
data class Button(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)

fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdateId = 0L
    val json = Json {
        ignoreUnknownKeys = true
    }
    val botService = TelegramBotService(botToken,json)

    val trainer = try {
        LearnWordTrainer(DICTIONARY_FILE, MIN_LEARNED)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        val responseString = botService.getUpdates(lastUpdateId)
        val response = json.decodeFromString<Response>(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val messageText = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id ?: continue
        val data = firstUpdate.callbackQuery?.data


        if (messageText == "Hello") botService.sendText(chatId, "Hello")
        if (messageText == "/start") botService.sendMenu(chatId)
        if (data == TelegramBotService.TO_STATISTICS_DATA) {
            botService.sendStatistics(chatId, trainer.getStatistics())
        }
        if (data == TelegramBotService.TO_LEARN_WORDS_DATA) {
            checkNextQuestionAndSend(
                trainer, botService, chatId
            )
        }
        if (data == TelegramBotService.TO_MENU_DATA) {
            botService.sendMenu(chatId)
        }

        if (data != null && data.startsWith(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX)) {
            val userAnswerIndex = data.substringAfter(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                botService.sendText(chatId, "Правильно!")
            } else {
                botService.sendText(
                    chatId,
                    """|Неправильно! 
                        |${trainer.question?.correctAnswer?.original} - это ${trainer.question?.correctAnswer?.translate}""".trimMargin()
                )
            }
            checkNextQuestionAndSend(trainer, botService, chatId)
        }

        Thread.sleep(3000)
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long
) {
    val question = trainer.getNextQuestion(TelegramBotService.ANSWER_VARIANTS_COUNT)
    if (question == null) telegramBotService.sendText(chatId, TelegramBotService.ALL_WORDS_LEARNED_TEXT)
    else telegramBotService.sendQuestion(chatId, question)
}

