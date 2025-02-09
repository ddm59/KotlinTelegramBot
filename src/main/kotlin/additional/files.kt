package additional

import java.io.File

const val PERCENT_MULTIPLIER = 100
const val CORRECT_ANSWER_LIMIT = 3
const val NUMBER_OF_QUESTION_WORDS = 4
const val DICTIONARY_FILE_PATH = "words.txt"
const val MENU_ITEMS = """
    Меню: 
    1 – Учить слова
    2 – Статистика
    0 – Выход
"""

fun main() {
    var isExit: Boolean = false
    val dictionary: MutableList<Word> = loadDictionary()

    while (!isExit) {
        var totalCount: Int
        var learnedCount: Int
        var percent: Int
        var notLearnedList: List<Word>
        var questionWords: List<Word>
        var correctAnswer: Word
        var userAnswer: Int = 0

        println(MENU_ITEMS.trimIndent())

        val userInput = readln().toIntOrNull() ?: 0

        when (userInput) {
            1 -> {
                notLearnedList = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWER_LIMIT }

                if (notLearnedList.isNotEmpty()) {

                    questionWords = notLearnedList.shuffled().take(4)
                    correctAnswer = questionWords[0].copy()

                    if (questionWords.size < NUMBER_OF_QUESTION_WORDS) {
                        questionWords =                                   // Дополнение списка выученными словами
                            questionWords.plus(dictionary.filter { it.correctAnswersCount > CORRECT_ANSWER_LIMIT }
                                .shuffled().take(NUMBER_OF_QUESTION_WORDS - questionWords.size))
                    }

                    println(correctAnswer.word)

                    questionWords.shuffled().forEachIndexed { index, word ->  //Вывод списка ответов
                        println("  ${index + 1} - ${word.translate}")
                    }

                    userAnswer = readln().toIntOrNull() ?: 0  // Ответ пользователя

                } else {
                    println("Все слова в словаре выучены")
                }
            }

            2 -> {
                totalCount = dictionary.size
                println("Общее количество слов : $totalCount")

                learnedCount = dictionary.filter { it.correctAnswersCount >= CORRECT_ANSWER_LIMIT }.count()
                println("Количество выученных слов: $learnedCount")

                percent = ((learnedCount.toDouble() / totalCount.toDouble()) * PERCENT_MULTIPLIER).toInt()
                println("Процентное соотношение: $percent%")

                println("Выучено $learnedCount из $totalCount слов | $percent%\n")
            }

            0 -> {
                println("Выход из меню")
                isExit = true
            }

            else -> println("Введите число 1, 2 или 0")
        }
    }
}

fun loadDictionary(): MutableList<Word> {
    val wordsFile: File = File(DICTIONARY_FILE_PATH)
    val dictionary: MutableList<Word> = mutableListOf()
    val lines: List<String> = wordsFile.readLines()

    for (line in lines) {
        val line = line.split("|")
        val word = Word(word = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }
    return dictionary
}

