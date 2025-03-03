package additional

import okhttp3.MediaType.Companion.toMediaType
import java.net.URLEncoder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

const val STATISTIC_BUTTON = "statistics_clicked"
const val WORDS_LEARN_BUTTON = "learn_words_clicked"
const val TG_BOT_URL = "https://api.telegram.org/bot"

class TelegramBotService(
    private val botToken: String,
) {
    private val  client: OkHttpClient = OkHttpClient()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TG_BOT_URL$botToken/getUpdates?offset=$updateId"
        val request = Request.Builder().url(urlGetUpdates).build()

        client.newCall(request).execute().use { response: Response ->
            return response.body?.string() ?: ""
        }
    }

    fun sendMessage(chatId: Long, message: String): String {
        val encodedMessage = URLEncoder.encode(message, Charsets.UTF_8.toString())
        val url = "$TG_BOT_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"

        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response: Response ->
            return response.body?.string() ?: ""
        }
    }

    fun sendMenu(chatId: Long): String {
        val url = "$TG_BOT_URL$botToken/sendMessage"
        val jsonBody = """{
        "chat_id": $chatId,
        "text": "Основное меню",
        "reply_markup": {
            "inline_keyboard": [
                [
                    {"text": "Изучить слова", "callback_data": $WORDS_LEARN_BUTTON},
                    {"text": "Статистика", "callback_data": $STATISTIC_BUTTON}
                ]
            ]
        }
    }""".trimIndent()

        val requestBody = RequestBody.create("application/json".toMediaType(), jsonBody)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response: Response ->
            return response.body?.string() ?: ""
        }
    }
}