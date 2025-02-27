package additional

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.URI
import java.net.http.HttpResponse

const val INDEX_INCREASE = 1
const val UPDATE_ID_REGEX_STRING = "\"update_id\":(.+?),"
const val MEASSAGE_REGEX_STRING = "\"text\":\"(.+?)\""

fun main(args: Array<String>) {
    val updateIdRegex = UPDATE_ID_REGEX_STRING.toRegex()
    val messageTextRegex = MEASSAGE_REGEX_STRING.toRegex()
    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)

        val messageMatchResult: MatchResult = messageTextRegex.find(updates) ?: continue
        println(messageMatchResult.groupValues[1])

        val idMatchResult: MatchResult = updateIdRegex.find(updates) ?: continue
        val updateIdString = idMatchResult.groupValues[1].toIntOrNull()?:0
        updateId = updateIdString + INDEX_INCREASE
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}