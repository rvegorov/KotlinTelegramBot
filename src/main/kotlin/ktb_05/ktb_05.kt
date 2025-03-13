package org.example.ktb_05

import java.io.File

fun main() {
    var dictionary = loadDictionary("words.txt")

    println(
        ("""Меню: 
            |1 – Учить слова
            |2 – Статистика
            |0 – Выход""").trimMargin()
    )

    while (true) {
        print("Выберите пункт меню: ")
        val selectedItem = readln().toInt()
        when(selectedItem){
            1 -> println("Вы выбрали \"Учить слова\"")
            2 -> println("Вы выбрали \"Статистика\"")
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