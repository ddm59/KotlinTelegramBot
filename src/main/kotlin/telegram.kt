import kotlinx.serialization.json.Json

const val INDEX_INCREASE = 1

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId: Long = 0
    val json = Json { ignoreUnknownKeys = true }
    val trainers = HashMap<Long, LearnWordsTrainer>()

    val telegramBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(3000)
        val responseString: String? = telegramBotService.getUpdates(lastUpdateId)
        val response: Response = json.decodeFromString(responseString?:"{\"result\":[]}")
        println(response.toString())
        if (response.result.isNullOrEmpty()) continue
        val sortUpdates = response.result.sortedBy { it.updateId }
        sortUpdates.forEach { handleUpdate(it, json, trainers, telegramBotService) }
        lastUpdateId = sortUpdates.last().updateId + INDEX_INCREASE

    }
}

fun handleUpdate(
    update: Update, json: Json, trainers: HashMap<Long, LearnWordsTrainer>, telegramBotService: TelegramBotService
) {
    val message = update.message?.text
    val data = update.callBackQuery?.data
    val chatId = update.message?.chat?.id ?: update.callBackQuery?.message?.chat?.id ?: return
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    when {
        message == START_COMMAND || data == START_COMMAND -> {
            telegramBotService.sendMenu(json, chatId)
        }

        data == STATISTIC_BUTTON -> {
            val statistics = trainer.getStatistics()
            val statisticsPrint =
                "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%"
            telegramBotService.sendMessage(json, chatId, statisticsPrint)
        }

        data == WORDS_LEARN_BUTTON -> {
            checkNextQuestionAndSend(
                json = Json, trainer = trainer, telegramBotService = telegramBotService, chatId = chatId
            )
        }

        data == RESET_BUTTON -> {
            trainer.resetProgress()
            telegramBotService.sendMessage(json, chatId, "Прогресс сброшен.")
        }

        data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
            val index = data?.substringAfter(CALLBACK_DATA_ANSWER_PREFIX)?.toInt()
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
                json = Json, trainer = trainer, telegramBotService = telegramBotService, chatId = chatId
            )
        }
    }
}