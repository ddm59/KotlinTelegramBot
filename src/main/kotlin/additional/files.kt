package additional

import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")

    wordsFile.readLines().forEach { println(it) }
}