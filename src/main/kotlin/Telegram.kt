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
    val botService = TelegramBotService(botToken, json)

    val trainers = HashMap<Long, LearnWordTrainer>()

    while (true) {
        val updates = botService.getUpdates(lastUpdateId)
        if (updates.isEmpty()) continue

        val sortedUpdates = updates.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, botService, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
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

fun handleUpdate(
    update: Update,
    botService: TelegramBotService,
    trainers: HashMap<Long, LearnWordTrainer>
) {
    val messageText = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordTrainer("$chatId.txt") }

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

    if (data == TelegramBotService.RESET_STATISTICS_DATA) {
        trainer.resetProgress()
        botService.sendText(chatId, "Прогресс сброшен")
    }

    if (data?.startsWith(TelegramBotService.CALLBACK_DATA_ANSWER_PREFIX) == true) {
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
}