package org.example.KTB_02

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    wordsFile.readLines().forEach { println(it) }
}