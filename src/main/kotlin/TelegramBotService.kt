import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

@Serializable
data class Update(
    @SerialName("update_id") val updateId: Long,
    @SerialName("message") val message: Message? = null,
    @SerialName("callback_query") val callBackQuery: CallBackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result") val result: List<Update>,
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
    @SerialName("id")
    val id: Long,
    @SerialName("username")
    val userName: String? = null,
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
    private val client: HttpClient = HttpClient.newBuilder().build()
    val json = Json { ignoreUnknownKeys = true }

    fun getUpdates(updateId: Long): Response{
        val urlGetUpdates = "$TG_BOT_URL$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: IOException) {
            return Response(listOf(Update(0L, null, null)))
        }
        val responseString: String = response.body()
        println(responseString)
        return json.decodeFromString(responseString)
    }

    fun sendMessage( chatId: Long, message: String): String {
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8)
        val urlSendMessage = "$TG_BOT_URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"

        val requestBody = SendMessageRequest(chatId, message)
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu( chatId: Long): String {
        val urlSendMessage = "$TG_BOT_URL$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId, "Главное меню", ReplyMarkup(
                listOf(
                    listOf(
                        InLineKeyBoard(
                            text = "Учить слова",
                            callbackData = WORDS_LEARN_BUTTON,
                        ),
                        InLineKeyBoard(
                            text = "Статистика",
                            callbackData = STATISTIC_BUTTON,
                        ),
                    ),
                    listOf(
                        InLineKeyBoard(
                            text = "Сбросить прогресс",
                            callbackData = RESET_BUTTON,
                        ),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(chatId: Long, question: Question): String {
        val urlSendMessage = "$TG_BOT_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId, question.correctAnswer.word, ReplyMarkup(
                (question.variants.mapIndexed { index, word ->
                    InLineKeyBoard(
                        text = word.translate,
                        callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                } + InLineKeyBoard(
                    text = "В главное меню",
                    callbackData = START_COMMAND
                )).chunked(2)
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun handleUpdate(
        update: Update, trainers: HashMap<Long, LearnWordsTrainer>
    ) {
        val userName = update.message?.chat?.userName
        val chatId = update.message?.chat?.id ?: update.callBackQuery?.message?.chat?.id ?: return
        val receivedText = update.message?.text
        val receivedData = update.callBackQuery?.data

        val dictionary = DatabaseUserDictionary(chatId)
        if (userName != null) {
            dictionary.loadDictionary(userName)
        }

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer(chatId) }

        if (receivedText == "/start".lowercase()) sendMenu(chatId)
        if (receivedData == STATISTIC_BUTTON) {
            val statistics = trainer.getStatistics()
            sendMessage(
                chatId,
                String.format(
                    "Выучено %d из %d слов | %d%%",
                    statistics.learnedCount,
                    statistics.totalCount,
                    statistics.percent
                )
            )
            sendMenu(chatId)
        }
        if (receivedData == WORDS_LEARN_BUTTON) checkNextQuestionAndSend(trainer, chatId)
        if (receivedData == START_COMMAND) sendMenu(chatId)
        if (receivedData?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val dataIndex = receivedData.substringAfter("_").toInt()
            if (trainer.checkAnswer(dataIndex)) {
                sendMessage(
                    chatId,
                    "Правильно!"
                )
            } else {
                sendMessage(
                    chatId,
                    "Неправильно! ${trainer.question?.correctAnswer?.word} - это ${trainer.question?.correctAnswer?.translate}"
                )
            }
            checkNextQuestionAndSend(trainer, chatId)
        }
        if (receivedData == RESET_BUTTON) {
            dictionary.resetUserProgress()
            sendMessage(
                chatId,
                "Прогресс сброшен"
            )
            sendMenu(chatId)
        }
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Long) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(
                chatId,
                "Все слова в базе выучены!"
            )
            sendMenu(chatId)
        } else sendQuestion(chatId, question)
    }

}

