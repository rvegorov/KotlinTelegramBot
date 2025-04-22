package org.example

fun main(args: Array<String>) {
    val botToken = args[0]

    val botService = TelegramBotService(botToken)

    var updateId = 0
    val updateRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val chatIdRegex = "(?s)(?:chat.*?id.*?:.*?)(.*?),".toRegex()
    val messageTextRegex = "(?s)(?:message.*?text.*?:.*?\")(.*?)(?:\")".toRegex()

    while (true) {
        val updates = botService.getUpdates(updateId)
        val updateIdMatchResult: MatchResult = updateRegex.find(updates) ?: continue
        val updateIdStr: String? = updateIdMatchResult.groups[1]?.value
        updateId = if (updateIdStr != null) updateIdStr.toInt() + 1 else updateId

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value.toString()
        val messageText = messageTextRegex.find(updates)?.groups?.get(1)?.value.toString()
        if (messageText == "Hello") botService.sendMessage(chatId, "Hello")

        println(messageText)
        println(updates)
        Thread.sleep(3000)
    }
}
