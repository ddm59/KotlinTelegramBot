package additional
import kotlinx.serialization.json.Json

const val INDEX_INCREASE = 1

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId: Long = 0
    val json = Json {
        ignoreUnknownKeys = true
    }

    val trainer = try {
        LearnWordsTrainer(CORRECT_ANSWER_LIMIT, NUMBER_OF_QUESTION_WORDS)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }
    val telegramBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + INDEX_INCREASE
        val message = firstUpdate.massage?.text
        val data = firstUpdate.callBackQuery?.data
        val chatId = firstUpdate.massage?.chat?.id ?: firstUpdate.callBackQuery?.message?.chat?.id

        when {
            message == START_COMMAND -> {
                if (chatId != null) {
                    telegramBotService.sendMenu(json, chatId)
                }
            }

            data == STATISTIC_BUTTON -> {
                val statistics = trainer.getStatistics()
                val statisticsPrint =
                    "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%"
                telegramBotService.sendMessage(json, chatId, statisticsPrint)
            }

            data == WORDS_LEARN_BUTTON -> {
                checkNextQuestionAndSend(
                    json = Json,
                    trainer = trainer,
                    telegramBotService = telegramBotService,
                    chatId = chatId
                )
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                val index = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
                if (trainer.checkAnswer(index)) {
                    telegramBotService.sendMessage(json, chatId, "Правильно!")
                } else {
                    telegramBotService.sendMessage(
                        json,
                        chatId,
                        "Неправильно. ${trainer.question?.correctAnswer?.word} - это ${trainer.question?.correctAnswer?.translate}."
                    )
                }
                checkNextQuestionAndSend(
                    json = Json,
                    trainer = trainer,
                    telegramBotService = telegramBotService,
                    chatId = chatId
                )
            }
        }
    }
}



