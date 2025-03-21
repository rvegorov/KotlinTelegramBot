package org.example.ktb_console

import java.io.File

fun main() {
    val dictionary = loadDictionary(DICTIONARY_FILE)

    while (true) {
        printMenu()
        print("Выберите пункт меню: ")
        val selectedItem = readln().toInt()
        when (selectedItem) {
            // Учить слова
            1 -> {
                println("\nВы выбрали \"Учить слова\"")
                val notLearnedList: List<Word> =
                    dictionary.filter { it.correctAnswersCount < MIN_LEARNED }

                if (notLearnedList.isEmpty()) {
                    println("Все слова в словаре выучены!")
                } else {
                    val questionWords = notLearnedList.shuffled().take(ANSWER_VARIANTS_COUNT)
                    val correctAnswer = questionWords.random()
                    val correctAnswerId = questionWords.indexOf(correctAnswer)
                    println("\n${correctAnswer.original}:")
                    questionWords.forEachIndexed { i, word ->
                        println(" ${i + 1} - ${word.translate}")
                    }
                    println(" ----------\n 0 - меню")

                    print("Ваш ответ: ")
                    val userAnswerInput = readln().toInt()

                    if (userAnswerInput == 0) {
                    } else if (userAnswerInput - 1 == correctAnswerId) {
                        correctAnswer.correctAnswersCount++
                        println("Правильно!\n")
                        saveDictionary(dictionary, DICTIONARY_FILE)
                    } else {
                        println("Не правильно! \"${correctAnswer.original}\" - это \"${correctAnswer.translate}\" \n")
                    }
                }
            }
            // Статистика
            2 -> {
                val learnedCount = dictionary.filter { it.correctAnswersCount >= MIN_LEARNED }.count()
                val totalCount = dictionary.count()
                val percent = learnedCount / totalCount * 100
                println("\n Статистика: \nВыучено $learnedCount из $totalCount | $percent%)\n")
            }
            // Выход
            0 -> break
        }
    }
}

fun loadDictionary(dictionaryFile: String): List<Word> {
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

fun saveDictionary(dictionary: List<Word>, dictionaryFile: String) {
    val dictionaryText = dictionary.joinToString("\n") { word ->
        "${word.original}|${word.translate}|${word.correctAnswersCount}"
    }

    File(dictionaryFile).writeText(dictionaryText)
}

const val MIN_LEARNED = 3
const val ANSWER_VARIANTS_COUNT = 4
const val DICTIONARY_FILE = "words.txt"