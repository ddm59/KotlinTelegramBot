package additional

import java.io.File

const val PERCENT_MULTIPLIER = 100

data class Word(
    val word: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val totalCount: Int,
    val learnedCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val correctAnswerLimit: Int = 3,
    private val numberOfQuestionWords: Int = 4
) {

    var question: Question? = null
        private set
    private val dictionary = loadDictionary()


    fun getStatistics(): Statistics {
        val totalCount = dictionary.size
        val learnedCount = dictionary.filter { it.correctAnswersCount >= correctAnswerLimit }.count()
        val percent = ((learnedCount.toDouble() / totalCount.toDouble()) * PERCENT_MULTIPLIER).toInt()
        return Statistics(totalCount, learnedCount, percent)
    }

    fun getNextQuestion(): Question? {

        val notLearnedList = dictionary.filter { it.correctAnswersCount < correctAnswerLimit }
        if (notLearnedList.isEmpty()) return null

        val questionWords = if (notLearnedList.size < numberOfQuestionWords) {
            val learnedList = dictionary.filter { it.correctAnswersCount > correctAnswerLimit }.shuffled()
            notLearnedList.shuffled().take(numberOfQuestionWords) +
                    learnedList.take(numberOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(numberOfQuestionWords)
        }.shuffled()

        val correctAnswer = questionWords.random()
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
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary: MutableList<Word> = mutableListOf()
            val lines: List<String> = wordsFile.readLines()
            for (line in lines) {
                val line = line.split("|")
                val word = Word(word = line[0], translate = line[1], correctAnswersCount = line[2].toIntOrNull() ?: 0)
                dictionary.add(word)
            }
            return dictionary.toList()
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Некорректный файл")
        }
    }

    private fun saveDictionary() {
        val wordsFile: File = File(fileName)
        val text = dictionary.joinToString(separator = "\n") { "${it.word}|${it.translate}|${it.correctAnswersCount}" }
        wordsFile.writeText(text)
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }
}

