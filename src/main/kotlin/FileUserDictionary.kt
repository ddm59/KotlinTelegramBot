import java.io.File
const val DEFAULT_FILE_NAME = "words.txt"


class FileUserDictionary(
    private val fileName: String = DEFAULT_FILE_NAME,
    private val learningThreshold: Int = DEFAULT_LEARNING_THRESHOLD,
) : IUserDictionary {

    private val dictionary = try {
        loadDictionary()
    } catch (e: Exception) {
        throw IllegalArgumentException("Некорректный файл")
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        dictionary.find { it.word == original }?.correctAnswersCount = correctAnswersCount
        saveDictionary()
    }

    override fun getNumOfLearnedWords(): Int{
        return dictionary.filter { it.correctAnswersCount >= learningThreshold }.count()
    }

    override fun getSize(): Int{
        return dictionary.size
    }

    override fun getLearnedWords(): List<Word>{
        return dictionary.filter { it.correctAnswersCount >= learningThreshold }
    }
    override fun getUnlearnedWords(): List<Word>{
        return dictionary.filter { it.correctAnswersCount < learningThreshold }
    }

    override fun resetUserProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
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
        val file = File(fileName)
        val newFileContent = dictionary.map { "${it.word}|${it.translate}|${it.correctAnswersCount}" }
        file.writeText(newFileContent.joinToString(separator = "\n"))
    }


}