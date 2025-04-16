package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val updateRegex: Regex = "\"update_id\":(.+?),".toRegex()

    while (true) {
        val updates = getUpdates(botToken, updateId)
        val matchResult: MatchResult = updateRegex.find(updates) ?: continue
        val updateIdStr: String? = matchResult.groups[1]?.value
        updateId = if (updateIdStr != null) updateIdStr.toInt() + 1 else updateId
        println(updates)
        println(updateId)

        Thread.sleep(3000)
    }

}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetMe = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder(URI.create(urlGetMe)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body()
}
