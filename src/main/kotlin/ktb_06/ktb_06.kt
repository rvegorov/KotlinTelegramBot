package org.example.ktb_06

import java.io.File

fun main() {
    var dictionary = loadDictionary("words.txt")

    printMenu()

    while (true) {
        print("Выберите пункт меню: ")
        val selectedItem = readln().toInt()
        when (selectedItem) {
            1 -> println("Вы выбрали \"Учить слова\"")
            2 -> {
                val learnedCount = dictionary.filter { it.correctAnswersCount >= 3 }.count()
                val totalCount = dictionary.count()
                val percent = (learnedCount / totalCount * 100).toInt()
                println( """Статистика:
                    |Выучено $learnedCount из $totalCount | $percent%""".trimMargin())
                println()
                printMenu()
            }

            0 -> break
        }
    }
}

fun loadDictionary(dictionaryFile: String): MutableList<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    File(dictionaryFile).forEachLine {
        val line = it.split("|")
        val cac = line[2].toIntOrNull() ?: 0
        val word = Word(line[0], line[1], cac)
        dictionary.add(word)
    }
    return dictionary
}

fun printMenu(){
    println(
        ("""Меню: 
            |1 – Учить слова
            |2 – Статистика
            |0 – Выход""").trimMargin()
    )
}