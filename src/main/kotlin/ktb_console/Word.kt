package org.example.ktb_06

data class Word (
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)