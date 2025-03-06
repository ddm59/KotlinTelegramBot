package additional

const val INDEX_INCREASE = 1
const val UPDATE_ID_REGEX_STRING = "\"update_id\":(\\d+),"
const val MEASSAGE_REGEX_STRING = "\"text\":\"(.+?)\""
const val CHAT_ID_REGEX_STRING = "\"chat\":\\{\"id\":(\\d+)"
const val DATA_ID_REGEX_STRING = "\"data\":\"(.+?)\""



fun main(args: Array<String>) {
    val updateIdRegex = UPDATE_ID_REGEX_STRING.toRegex()
    val messageTextRegex = MEASSAGE_REGEX_STRING.toRegex()
    val chatIdRegex = CHAT_ID_REGEX_STRING.toRegex()
    val dataRegex = DATA_ID_REGEX_STRING.toRegex()
    val botToken = args[0]
    var lastUpdateId = 0

    val trainer = try {
        LearnWordsTrainer(CORRECT_ANSWER_LIMIT, NUMBER_OF_QUESTION_WORDS)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }
    val telegramBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)

        val updates: String = telegramBotService.getUpdates(lastUpdateId)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toInt() ?: continue
        lastUpdateId = updateId + INDEX_INCREASE

        val message = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toLongOrNull() ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (message?.lowercase() == "/start") {
            telegramBotService.sendMenu(chatId)
        }
        if (data?.lowercase() == WORDS_LEARN_BUTTON) {
            checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }

        if (data?.lowercase() == STATISTIC_BUTTON ) {
            val statistics = trainer.getStatistics()
            telegramBotService.sendMessage(
                chatId,
                "Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%\n"
            )
        }

        if (data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter("_").toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                telegramBotService.sendMessage(chatId, "Правильно!")
                checkNextQuestionAndSend(trainer, telegramBotService, chatId)
            } else {
                val questionOriginal = trainer.question?.correctAnswer?.word
                val correctAnswer = trainer.question?.correctAnswer?.translate
                telegramBotService.sendMessage(
                    chatId,
                    "Неправильно! " +
                            "$questionOriginal это $correctAnswer.",

                    )
                checkNextQuestionAndSend(trainer, telegramBotService, chatId)
            }
        }
    }


}
private fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, telegramBotService: TelegramBotService, chatId: Long) {
    val nextQuestion = trainer.getNextQuestion()
    if (nextQuestion == null) {
        telegramBotService.sendMessage(chatId, "Вы выучили все слова в базе")
    } else {
        telegramBotService.sendQuestion(chatId, nextQuestion)
    }
}


