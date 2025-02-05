package additional

import java.io.File

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

        println(MENU_ITEMS.trimIndent())

        val userInput = readln().toIntOrNull() ?: 0

        when (userInput) {
            1 -> {
                println("Выбран пункт меню: 1 - Учить слова")
            }

            2 -> {
                println("Выбран пункт меню: 2 - Статистика")
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