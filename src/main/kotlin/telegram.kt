import kotlinx.serialization.json.Json

const val INDEX_INCREASE = 1

fun main(args: Array<String>) {

    var lastUpdateId: Long = 0
    val trainers = HashMap<Long, LearnWordsTrainer>()
    val botService = TelegramBotService(args[0])

    while (true) {
        Thread.sleep(2000)
        val updates = botService.getUpdates(lastUpdateId).result
        if (updates.isEmpty()) continue
        val sortedUpdates = updates.sortedBy { it.updateId }
        sortedUpdates.forEach { botService.handleUpdate(it, trainers) }
        lastUpdateId = sortedUpdates.last().updateId +  INDEX_INCREASE

    }
}

