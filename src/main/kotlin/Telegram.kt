package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId: Int = 0

    while (true) {
        val updates = getUpdates(botToken, updateId)
        val startUpdateId = updates.lastIndexOf("update_id") + 11
        val endUpdateId = updates.lastIndexOf(",\n\"message\"")
        if (startUpdateId == -1 || endUpdateId == -1) continue
        val updateIdStr = updates.substring(startUpdateId, endUpdateId)
        updateId = updateIdStr.toInt() + 1
        println(updates)
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
