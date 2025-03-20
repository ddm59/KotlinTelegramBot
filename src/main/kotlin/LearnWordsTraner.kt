import java.io.File
import kotlin.math.roundToInt

const val PERCENT_MULTIPLIER = 100.0

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
    chatId: Long,
    private val numberOfQuestionWords: Int = 4,
) {
    var question: Question? = null
    private val dictionary = DatabaseUserDictionary(chatId)


    fun getStatistics(): Statistics {
        val learnedWords = dictionary.getLearnedWords().size
        val totalCount = dictionary.getSize()
        val percent = if (totalCount > 0) {
            (learnedWords.toDouble() / totalCount.toDouble() * PERCENT_MULTIPLIER).roundToInt()
        } else {
            0
        }

        return Statistics(totalCount , learnedWords, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.getUnlearnedWords()
        if (notLearnedList.isEmpty()) return null

        val questionWords = if (notLearnedList.size < numberOfQuestionWords) {
            val learnedList = dictionary.getLearnedWords()
            notLearnedList.shuffled().take(numberOfQuestionWords) + learnedList.shuffled()
                .take(numberOfQuestionWords - notLearnedList.size)
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
                dictionary.setCorrectAnswersCount(it.correctAnswer.word, it.correctAnswer.correctAnswersCount.plus(1))
                true
            } else false
        } ?: false
    }
}