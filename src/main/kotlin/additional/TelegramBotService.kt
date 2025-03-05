package additional

import okhttp3.MediaType.Companion.toMediaType
import java.net.URLEncoder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

const val STATISTIC_BUTTON = "statistics_clicked"
const val WORDS_LEARN_BUTTON = "learn_words_clicked"
private const val TG_BOT_URL = "https://api.telegram.org/bot"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(
    private val botToken: String,
) {
    private val client: OkHttpClient = OkHttpClient()

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

        return sendRequest(url)
    }

    fun sendMenu(chatId: Long): String {
        val url = "$TG_BOT_URL$botToken/sendMessage"
        val jsonBody = """{
        "chat_id": $chatId,
        "text": "Основное меню",
        "reply_markup": {
            "inline_keyboard": [
                [
                    {"text": "Изучить слова", "callback_data": "$WORDS_LEARN_BUTTON"},
                    {"text": "Статистика", "callback_data": "$STATISTIC_BUTTON"}
                ]
            ]
        }
    }""".trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response: Response ->
            return response.body?.string() ?: ""
        }
    }

    fun sendQuestion(chatId: Long, question: Question): String {
        val urlSendMessage = "$TG_BOT_URL$botToken/sendMessage"
        val keyboardButtons = question.variants.mapIndexed { index, word ->
            mapOf(
                "translation" to word.translate,
                "callback_data" to "$CALLBACK_DATA_ANSWER_PREFIX${index + 1}"
            )
        }

        val sendQuestionBody = """
            {
                "chat_id": $chatId,
                "text": "${question.correctAnswer.word}",
                "reply_markup": 
                {
                    "inline_keyboard": 
                    [
                        ${
            keyboardButtons.chunked(2).joinToString(",")
            { row ->
                row.joinToString(",", prefix = "[", postfix = "]")
                { button ->
                    "{\"text\": \"${button["translation"]}\", \"callback_data\": \"${button["callback_data"]}\"}"
                }
            }
        }
                    ]
                }
            }
        """.trimIndent()

        return sendRequest(urlSendMessage, sendQuestionBody)
    }

    private fun sendRequest(url: String, body: String? = null): String {
        if (body != null) {
            val requestBody = body.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response: Response ->
                return response.body?.string() ?: ""
            }
        }
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response: Response ->
            return response.body?.string() ?: ""
        }

    }

}