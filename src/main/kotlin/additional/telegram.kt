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

    val trainer =  try {
        LearnWordsTrainer(CORRECT_ANSWER_LIMIT, NUMBER_OF_QUESTION_WORDS)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        Thread.sleep(2000)
        val telegramBotService = TelegramBotService(botToken)
        val updates: String = telegramBotService.getUpdates(lastUpdateId)

        val updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toInt() ?: continue
        lastUpdateId = updateId + INDEX_INCREASE

        val message = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value?.toLong()
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (message?.lowercase() == "/start" && chatId != null) {
            telegramBotService.sendMenu(chatId)
        }
        if (data?.lowercase() == "statistics_clicked" && chatId != null) {
            telegramBotService.sendMessage(chatId, "Выучено 10 из 10 слов | 100%")
        }
    }
}


