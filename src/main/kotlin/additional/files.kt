package additional

import java.io.File


const val DICTIONARY_FILE_PATH = "words.txt"
const val MENU_ITEMS = """
    Меню: 
    1 – Учить слова
    2 – Статистика
    0 – Выход
"""
const val EXIT_ITEM = """------------
 0 - Меню"""


fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index, word -> " ${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.word + "\n" + variants + "\n $EXIT_ITEM"
}

fun main() {

    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        println(MENU_ITEMS.trimIndent())

        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Все слова в словаре выучены")
                        break
                    } else {
                        println(question.asConsoleString())
                    }
                    var userAnswerInput = readln().toIntOrNull()
                    if (userAnswerInput == 0) break

                    if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                        println("Правильно!\n")
                    } else {
                        println("Неправильно! ${question.correctAnswer.word} – это ${question.correctAnswer.translate}\n")
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learnedCount} из ${statistics.totalCount} слов | ${statistics.percent}%\n")
            }

            0 -> {
                println("Выход из меню")
                break
            }

            else -> println("Введите число 1, 2 или 0")
        }
    }
}



