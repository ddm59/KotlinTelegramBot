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
const val EXIT_ITEM = """  ------------
  0 - Меню"""

fun main() {

    while (true) {
        val dictionary = loadDictionary()

        println(MENU_ITEMS.trimIndent())
        val userInput = readln().toIntOrNull() ?: 0

        when (userInput) {
            1 -> {
                val notLearnedList = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWER_LIMIT }

                if (notLearnedList.isNotEmpty()) {

                    var questionWords = notLearnedList.shuffled().take(NUMBER_OF_QUESTION_WORDS)
                    val correctAnswer = questionWords.random()

                    if (questionWords.size < NUMBER_OF_QUESTION_WORDS) {
                        questionWords =                                   // Дополнение списка выученными словами
                            questionWords.plus(dictionary.filter { it.correctAnswersCount > CORRECT_ANSWER_LIMIT }
                                .shuffled().take(NUMBER_OF_QUESTION_WORDS - questionWords.size))
                    }

                    println(correctAnswer.word)
                    questionWords.forEachIndexed { index, word ->  //Вывод списка ответов
                        println("  ${index + 1} - ${word.translate}")
                    }
                    println(EXIT_ITEM)

                    val correctAnswerId = questionWords.indexOf(correctAnswer)
                    var userAnswerInput = readln().toIntOrNull() ?: 0  // Ответ пользователя

                    if (userAnswerInput != 0) {
                        if (--userAnswerInput == correctAnswerId) {
                            println("Правильно!\n")
                            questionWords[correctAnswerId].correctAnswersCount++
                            saveDictionary(dictionary)
                        } else {
                            println("Неправильно! ${correctAnswer.word} – это ${correctAnswer.translate}\n")
                        }
                    }
                } else {
                    println("Все слова в словаре выучены")

                }
            }

            2 -> {
                val totalCount = dictionary.size
                println("Общее количество слов : $totalCount")

                val learnedCount = dictionary.filter { it.correctAnswersCount >= CORRECT_ANSWER_LIMIT }.count()
                println("Количество выученных слов: $learnedCount")

                val percent = ((learnedCount.toDouble() / totalCount.toDouble()) * PERCENT_MULTIPLIER).toInt()
                println("Процентное соотношение: $percent%")
                println("Выучено $learnedCount из $totalCount слов | $percent%\n")
            }

            0 -> {
                println("Выход из меню")
                break
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

fun saveDictionary(dictionary: List<Word>) {
    val wordsFile: File = File(DICTIONARY_FILE_PATH)
    val text = dictionary.joinToString(separator = "\n") { "${it.word}|${it.translate}|${it.correctAnswersCount}" }
    wordsFile.writeText(text)
}

