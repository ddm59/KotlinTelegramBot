import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.File
import java.sql.Connection

fun main() {

    val formatter = DateTimeFormatter.ofPattern("yyyy-dd-MM HH:mm:ss")
    val currentDate: String = LocalDateTime.now().format(formatter)

    resetProgress()
    loadDictionary("HattoriHandzo", currentDate, 1254288685)
}

fun loadDictionary(userName: String, currentDate: String, chatId: Long) {
    DriverManager.getConnection(DATABASE_FILE_NAME)
        .use { connection ->
            val statement = connection.createStatement()

            // Create 'users' table
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS 'users' (
                    'id' INTEGER PRIMARY KEY AUTOINCREMENT,
                    'username' VARCHAR,
                    'created_at' TIMESTAMP,
                    'chat_id' BIGINT
                )
                """.trimIndent()
            )
            // Create 'words' table
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS 'words' (
                    'id' INTEGER PRIMARY KEY AUTOINCREMENT,
                    'text' VARCHAR NOT NULL,
                    'translate' VARCHAR NOT NULL
                )
                """.trimIndent()
            )
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS 'user_answers' (
                    'user_id' INTEGER,
                    'word_id' INTEGER,
                    'correct_answer_count' INTEGER DEFAULT 0,
                    'updated_at' TIMESTAMP,
                    FOREIGN KEY('user_id') REFERENCES 'users'('id'),
                    FOREIGN KEY('word_id') REFERENCES 'words'('id')
                )
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
            updateDictionary(File(DEFAULT_FILE_NAME))

        }
}

fun resetProgress() {
    DriverManager.getConnection(DATABASE_FILE_NAME)
        .use { connection ->
            val statement = connection.createStatement()
            statement.executeUpdate(
                """
                    DELETE FROM user_answers
                    WHERE user_id = 2;
            """.trimIndent()
            )
        }
}



fun updateDictionary(wordsFile: File) {
    DriverManager.getConnection(DATABASE_FILE_NAME)
        .use { connection ->
            val statement = connection.createStatement()
            wordsFile.readLines().forEach {
                val line = it.split("|")
                statement.executeUpdate(
                    String.format(
                        "insert into words values(null, \'%s\', \'%s\')",
                        line[0],
                        line[1],
                    )

                )
            }
        }
}
