package org.example.ktb_04

import java.io.File

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFile = File("words.txt")

    wordsFile.forEachLine {
        val line = it.split("|")
        val cac = line[2].toIntOrNull() ?: 0
        val word = Word(line[0], line[1], cac)
        dictionary.add(word)
    }

    dictionary.forEach { println(it) }
}
