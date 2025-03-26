package org.example.ktb_console

data class Word (
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)