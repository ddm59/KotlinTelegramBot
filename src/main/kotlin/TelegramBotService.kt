import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Serializable
data class Update(
    @SerialName("update_id") val updateId: Long,
    @SerialName("message") val message: Message? = null,
    @SerialName("callback_query") val callBackQuery: CallBackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result") val result: List<Update>?,
)

@Serializable
data class Message(
    @SerialName("text") val text: String,
    @SerialName("chat") val chat: Chat,
)

@Serializable
data class CallBackQuery(
    @SerialName("data") val data: String? = null,
    @SerialName("message") val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id") val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id") val chatId: Long?,
    @SerialName("text") val text: String,
    @SerialName("reply_markup") val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard") val inlineKeyboard: List<List<InLineKeyBoard>>,
)

@Serializable
data class InLineKeyBoard(
    @SerialName("callback_data") val callbackData: String,
    @SerialName("text") val text: String,
)


const val START_COMMAND = "/start"
const val STATISTIC_BUTTON = "statistics_clicked"
const val WORDS_LEARN_BUTTON = "learn_words_clicked"
private const val TG_BOT_URL = "https://api.telegram.org/bot"
const val RESET_BUTTON = "reset_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(private val botToken: String) {
    private val apiUrl = "$TG_BOT_URL${this.botToken}"
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): String? {
        val urlGetUpdates = "$apiUrl/getUpdates?offset=$updateId"
        try{
            val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
            val responseGetUpdates: HttpResponse<String> =
                client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())
            return responseGetUpdates.body()
        }catch (e: Exception){
            println(e.message)
        }
        return null
    }

    fun sendMessage(json: Json, chatId: Long?, message: String): String? {
        val urlSendMessage = "$apiUrl/sendMessage?"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )

        val requestBodyString = json.encodeToString(requestBody)

        try {
            val requestSendMessage: HttpRequest =
                HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).header("Content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyString)).build()
            val responseSendMessage: HttpResponse<String> =
                client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
            return responseSendMessage.body()
        }catch (e: Exception){
            println(e.message)
        }
        return null
    }

    fun sendMenu(json: Json, chatId: Long): String? {
        val urlSendMessage = "$apiUrl/sendMessage?"

        val requestBody = SendMessageRequest(
            chatId = chatId, text = "Основное меню", replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InLineKeyBoard(text = "Изучать слова", callbackData = WORDS_LEARN_BUTTON),
                        InLineKeyBoard(text = "Статистика", callbackData = STATISTIC_BUTTON),
                    ), listOf(InLineKeyBoard(text = "Сбросить прогресс", callbackData = RESET_BUTTON))
                )
            )
        )


        val requestBodyString = json.encodeToString(requestBody)

       try {
           val requestSendMessage: HttpRequest =
               HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).header("Content-type", "application/json")
                   .POST(HttpRequest.BodyPublishers.ofString(requestBodyString)).build()
           val responseSendMessage: HttpResponse<String> =
               client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
           return responseSendMessage.body()
       }catch (e: Exception){
           println(e.message    )
       }
        return null
    }

    fun sendQuestion(json: Json, chatId: Long?, question: Question): String? {
        val urlSendMessage = "$apiUrl/sendMessage?"

        val requestBody = SendMessageRequest(
            chatId = chatId, text = question.correctAnswer.word, replyMarkup = ReplyMarkup(
                listOf(
                    question.variants.mapIndexed { index: Int, word: Word ->
                        InLineKeyBoard(text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index")
                    }, listOf(InLineKeyBoard(text = "Возврат в главное меню", callbackData = START_COMMAND))
                ),
            )
        )

        val requestBodyString = json.encodeToString(requestBody)

        try {
            val requestSendMessage: HttpRequest =
                HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).header("Content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyString)).build()
            val responseSendMessage: HttpResponse<String> =
                client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
            return responseSendMessage.body()
        }catch (e: Exception){
            println(e.message)
        }
        return null
    }
}

fun checkNextQuestionAndSend(
    json: Json, trainer: LearnWordsTrainer, telegramBotService: TelegramBotService, chatId: Long?
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(json, chatId, "Вы выучили все слова в базе")
    } else {
        telegramBotService.sendQuestion(json, chatId, question)
    }
}