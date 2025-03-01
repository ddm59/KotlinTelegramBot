package additional

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val  TG_BOT_URL = "https://api.telegram.org/bot"


class TelegramBotService(
    private val botToken: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TG_BOT_URL$botToken/getUpdates?offset=$updateId"
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, message: String): String {
        val encodedMessage = URLEncoder.encode(message, Charsets.UTF_8.toString())
        val sendMessage =
            "$TG_BOT_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

}