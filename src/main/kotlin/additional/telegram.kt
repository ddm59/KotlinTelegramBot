package additional

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.URI
import java.net.http.HttpResponse

const val SYMBOL_COUNT = 11
const val INDEX_NOT_FOUND = -1
const val INDEX_INCREASE = 1

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)
        val startUpdateId = updates.lastIndexOf("update_id")
        val endUpdateId = updates.lastIndexOf(",\n\"message\"")

        if (startUpdateId == INDEX_NOT_FOUND || endUpdateId == INDEX_NOT_FOUND) continue
        val updateIdString = updates.substring(startUpdateId + SYMBOL_COUNT, endUpdateId)
        updateId = updateIdString.toInt() + INDEX_INCREASE
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}