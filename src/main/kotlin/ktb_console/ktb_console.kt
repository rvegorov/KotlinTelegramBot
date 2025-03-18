package org.example.ktb_06

import java.io.File

fun main() {
    var dictionary = loadDictionary("words.txt")

    printMenu()

    while (true) {
        print("Выберите пункт меню: ")
        val selectedItem = readln().toInt()
        when (selectedItem) {
            // Учить слова
            1 -> {
                println("\nВы выбрали \"Учить слова\"")
                val notLearnedList: MutableList<Word> =
                    dictionary.filter { it.correctAnswersCount < MIN_LEARNED }.toMutableList()

                if (notLearnedList.isEmpty()) {
                    println("Все слова в словаре выучены!")
                    printMenu()
                } else {
                    val questionWords = notLearnedList.shuffled().take(ANSWER_VARIANTS_COUNT)
                    val rightWord = questionWords.random()
                    println()
                    println("${rightWord.original}:")
                    questionWords.forEachIndexed { i, word ->
                        println("$i - ${word.translate}")
                    }

                    print("Ваш ответ: ")
                    val userAnswer = readln().toInt()
                }
            }
            // Статистика
            2 -> {
                val learnedCount = dictionary.filter { it.correctAnswersCount >= MIN_LEARNED }.count()
                val totalCount = dictionary.count()
                val percent = learnedCount / totalCount * 100
                println("\n Статистика: \nВыучено $learnedCount из $totalCount | $percent%)\n")
                printMenu()
            }
            // Выход
            0 -> break
        }
    }
}

fun loadDictionary(dictionaryFile: String): MutableList<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    File(dictionaryFile).forEachLine {
        val line = it.split("|")
        val word = Word(line[0], line[1], line[2].toIntOrNull() ?: 0)
        dictionary.add(word)
    }
    return dictionary
}

fun printMenu() {
    println(
        ("""Меню: 
            |1 – Учить слова
            |2 – Статистика
            |0 – Выход""").trimMargin()
    )
}

fun MutableList<Word>.countLearned(): Int {
    return this.filter { it.correctAnswersCount >= MIN_LEARNED }.count()
}

const val MIN_LEARNED = 3
const val ANSWER_VARIANTS_COUNT = 4