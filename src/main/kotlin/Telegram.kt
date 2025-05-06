package org.example

fun main(args: Array<String>) {
    val botToken = args[0]

    val botService = TelegramBotService(botToken)

    var updateId = 0
    val updateRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val chatIdRegex = "(?s)(?:chat.*?id.*?:.*?)(.*?),".toRegex()
    val messageTextRegex = "(?s)(?:message.*?text.*?:.*?\")(.*?)(?:\")".toRegex()
    val dataRegex: Regex = "(?s)(?:\"data\".*?)\"(.*?)\"".toRegex()

    val trainer = try {
        LearnWordTrainer(DICTIONARY_FILE, MIN_LEARNED)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        val updates = botService.getUpdates(updateId)
        val updateIdMatchResult: MatchResult = updateRegex.find(updates) ?: continue
        val updateIdStr: String? = updateIdMatchResult.groups[1]?.value
        updateId = if (updateIdStr != null) updateIdStr.toInt() + 1 else updateId

        val chatIdMatch = chatIdRegex.find(updates) ?: continue
        val chatId = chatIdMatch.groups[1]?.value?.toIntOrNull() ?: continue

        val messageTextMatch = messageTextRegex.find(updates) ?: continue
        val messageText = messageTextMatch.groups[1]?.value ?: continue

        val data = dataRegex.find(updates)?.groups?.get(1)?.value


        if (messageText == "Hello") botService.sendMessage(chatId, "Hello")
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
                botService.sendMessage(chatId, "Правильно!")
            } else {
                botService.sendMessage(
                    chatId,
                    """|Неправильно! 
                        |${trainer.question?.correctAnswer?.original} - это ${trainer.question?.correctAnswer?.translate}""".trimMargin()
                )
            }
            checkNextQuestionAndSend(trainer, botService, chatId)
        }

        println(messageText)
        println(updates)
        Thread.sleep(3000)
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordTrainer,
    telegramBotService: TelegramBotService,
    chatId: Int
) {
    val question = trainer.getNextQuestion(TelegramBotService.ANSWER_VARIANTS_COUNT)
    if (question == null) telegramBotService.sendMessage(chatId, TelegramBotService.ALL_WORDS_LEARNED_TEXT)
    else telegramBotService.sendQuestion(chatId, question)
}
