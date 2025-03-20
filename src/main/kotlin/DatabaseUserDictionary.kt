import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val DEFAULT_LEARNING_THRESHOLD = 3
const val DATABASE_FILE_NAME = "jdbc:sqlite:data.db"

class DatabaseUserDictionary(
    private val chatId: Long,
    private val learningThreshold: Int = DEFAULT_LEARNING_THRESHOLD,
) : IUserDictionary {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-dd-MM HH:mm:ss")

    fun loadDictionary(userName: String) {
        val wordsFile = File(DEFAULT_FILE_NAME)
        val currentDate: String = LocalDateTime.now().format(formatter)
        DriverManager.getConnection(DATABASE_FILE_NAME)
            .use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS 'users' (
	                'id'	integer DEFAULT 0,
	                'username'	varchar,
	                'created_at'	timestamp,
	                'chat_id'	bigint,
	                PRIMARY KEY('id' AUTOINCREMENT));

                    CREATE TABLE IF NOT EXISTS 'user_answers' (
	                'user_id'	integer DEFAULT 0,
	                'word_id'	integer,
	                'correct_answer_count'	integer DEFAULT 0,
	                'updated_at'	timestamp,
	                FOREIGN KEY('user_id') REFERENCES 'users'('id'),
	                FOREIGN KEY('word_id') REFERENCES 'words'('id'));
                
                    CREATE TABLE IF NOT EXISTS 'words' (
	                'id'	integer PRIMARY KEY AUTOINCREMENT,
	                'text'	varchar NOT NULL,
	                'translate'	varchar NOT NULL
                     );
                     """.trimIndent()
                )
                statement.executeUpdate(
                    """
                        INSERT OR IGNORE INTO users(username, created_at, chat_id)
                        VALUES(
                        '$userName', 
                        '$currentDate', 
                        $chatId
                        );
                    """.trimIndent()
                )
                updateDictionary(wordsFile)

                for (i in 0..getSize()) {
                    statement.executeUpdate(
                        """
                            INSERT INTO user_answers (user_id, word_id, updated_at)
                            VALUES(
                            (SELECT id FROM users WHERE chat_id = $chatId),
                            $i,
                            '$currentDate'
                            );
                        """.trimIndent()
                    )
                }
            }
    }

    override fun getNumOfLearnedWords(): Int {
        var counter = 0
        DriverManager.getConnection(DATABASE_FILE_NAME)
            .use { connection ->
                val statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery(
                    """
                    SELECT count(correct_answer_count)
                    FROM user_answers
                    WHERE correct_answer_count >= 3;
                """.trimIndent()
                )
                while (rs.next()) {
                    counter = rs.getInt("count(correct_answer_count)")
                }
            }
        return counter
    }

    override fun getSize(): Int {
        var counter = 0
        DriverManager.getConnection(DATABASE_FILE_NAME)
            .use { connection ->
                val statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery(
                    """
                    SELECT count(*)
                    FROM words;
                """.trimIndent()
                )
                while (rs.next()) {
                    counter = rs.getInt("count(*)")
                }
            }
        return counter
    }

    override fun getLearnedWords(): List<Word> {
        val learnedWords = mutableListOf<Word>()
        DriverManager.getConnection(DATABASE_FILE_NAME)
            .use { connection ->
                val statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery(
                    """
                    SELECT words.text, words.translate, user_answers.correct_answer_count from words
                    JOIN user_answers
                    ON words.id = user_answers.word_id
                    WHERE correct_answer_count >= $learningThreshold and user_id = 
                    (SELECT id from users WHERE chat_id = $chatId);
                """.trimIndent()
                )
                while (rs.next()) {
                    learnedWords.add(
                        Word(
                            rs.getString("text"),
                            rs.getString("translate"),
                            rs.getInt("correct_answer_count")
                        )
                    )
                }
            }
        return learnedWords
    }

    override fun getUnlearnedWords(): List<Word> {
        val unlearnedWords = mutableListOf<Word>()
        DriverManager.getConnection(DATABASE_FILE_NAME)
            .use { connection ->
                val statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery(
                    """
                    SELECT words.text, words.translate, user_answers.correct_answer_count from words
                    JOIN user_answers
                    ON words.id = user_answers.word_id
                    where correct_answer_count < $learningThreshold and user_id = 
                    (SELECT id from users WHERE chat_id = $chatId);
                """.trimIndent()
                )
                while (rs.next()) {
                    unlearnedWords.add(
                        Word(
                            rs.getString("text"),
                            rs.getString("translate"),
                            rs.getInt("correct_answer_count")
                        )
                    )
                }
            }
        return unlearnedWords
    }

    override fun setCorrectAnswersCount(word: String, correctAnswersCount: Int) {
        val currentDate: String = LocalDateTime.now().format(formatter)
        DriverManager.getConnection(DATABASE_FILE_NAME)
            .use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate(
                    """
                    UPDATE user_answers
                        SET correct_answer_count = $correctAnswersCount, updated_at = '$currentDate'
                        WHERE word_id = (
                        SELECT id FROM words
                        WHERE text = '$word' AND user_id = (
                        SELECT id FROM users
                        WHERE chat_id = $chatId));
            """.trimIndent()
                )
            }

    }

    override fun resetUserProgress() {
        DriverManager.getConnection(DATABASE_FILE_NAME)
            .use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate(
                    """
                    UPDATE user_answers
                    SET correct_answer_count = 0
                    WHERE user_id = (
                    SELECT id FROM users WHERE chat_id = $chatId);
            """.trimIndent()
                )
            }
    }
    fun updateDictionary( wordsFile: File) {
        val connection = DriverManager.getConnection(DATABASE_FILE_NAME)

        synchronized(connection) {
            val insertStatement = connection.prepareStatement("INSERT OR IGNORE INTO words (text, translate) VALUES (?, ?)")
            connection.autoCommit = false
            try {
                wordsFile.forEachLine { line ->
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size != 2) {
                        println("Некорректная строка: $line")
                        return@forEachLine
                    }
                    insertStatement.apply {
                        setString(1, parts[0])
                        setString(2, parts[1])
                        addBatch()
                    }
                }
                insertStatement.executeBatch()
                connection.commit()
            } catch (e: Exception) {
                connection.rollback()
                throw RuntimeException("Ошибка при загрузке словаря: ${e.message}", e)
            } finally {
                insertStatement.close()
                connection.autoCommit = true
            }
        }
    }
}