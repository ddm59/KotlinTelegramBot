package additional

data class Word(
    val word: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)