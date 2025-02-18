package additional

import java.io.File

const val PERCENT_MULTIPLIER = 100
const val CORRECT_ANSWER_LIMIT = 3
const val NUMBER_OF_QUESTION_WORDS = 4

data class Statistics(
    val totalCount: Int,
    val learnedCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {

    private var question: Question? = null
    private val dictionary = loadDictionary()


    fun getStatistics(): Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.filter { it.correctAnswersCount >= CORRECT_ANSWER_LIMIT }.count()
        val percent = ((learnedCount.toDouble() / totalCount.toDouble()) * PERCENT_MULTIPLIER).toInt()
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWER_LIMIT }
        if (notLearnedList.isEmpty()) {
            return null
        }
        var questionWords = notLearnedList.shuffled().take(NUMBER_OF_QUESTION_WORDS)
        val correctAnswer = questionWords.random()
        if (questionWords.size < NUMBER_OF_QUESTION_WORDS) {
            questionWords =                                   // Дополнение списка выученными словами
                questionWords.plus(dictionary.filter { it.correctAnswersCount > CORRECT_ANSWER_LIMIT }
                    .shuffled().take(NUMBER_OF_QUESTION_WORDS - questionWords.size))
        }
        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerId: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerId) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        val wordsFile: File = File(DICTIONARY_FILE_PATH)
        val dictionary: MutableList<Word> = mutableListOf()
        val lines: List<String> = wordsFile.readLines()
        for (line in lines) {
            val line = line.split("|")
            val word = Word(word = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
            dictionary.add(word)
        }
        return dictionary.toList()
    }

    private fun saveDictionary(dictionary: List<Word>) {
        val wordsFile: File = File(DICTIONARY_FILE_PATH)
        val text = dictionary.joinToString(separator = "\n") { "${it.word}|${it.translate}|${it.correctAnswersCount}" }
        wordsFile.writeText(text)
    }
}