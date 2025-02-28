package additional

const val INDEX_INCREASE = 1
const val UPDATE_ID_REGEX_STRING = "\"update_id\":(.+?),"
const val MEASSAGE_REGEX_STRING = "\"text\":\"(.+?)\""
const val CHAT_ID_REGEX_STRING = "\"chat\"\\s*:\\s*[^}]*\"id\"\\s*:\\s*(\\d+)"


fun main(args: Array<String>) {
    val updateIdRegex = UPDATE_ID_REGEX_STRING.toRegex()
    val messageTextRegex = MEASSAGE_REGEX_STRING.toRegex()
    val chatIdRegex = CHAT_ID_REGEX_STRING.toRegex()
    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val telegramBotService = TelegramBotService(botToken)
        val updates: String = telegramBotService.getUpdates(botToken, updateId)

        val messageMatchResult: MatchResult = messageTextRegex.find(updates) ?: continue

        val idMatchResult: MatchResult = updateIdRegex.find(updates) ?: continue
        val updateIdString = idMatchResult.groupValues[1].toIntOrNull()?:0
        updateId = updateIdString + INDEX_INCREASE

        val chatIdMatchResult: MatchResult = chatIdRegex.find(updates) ?: continue
        val chatId = chatIdMatchResult.groupValues[1].toIntOrNull()?:0

        if(messageMatchResult.groupValues[1].lowercase() == "hello") {
            val response =  telegramBotService.sendMessage(chatId, messageMatchResult.groupValues[1])
        }
    }
}


