package org.example.ktb_console

fun Question.toConsoleString(): String {
    return  "\n${correctAnswer.original}:\n" +
            variants.mapIndexed { i, word ->
                " ${i + 1} - ${word.translate}"
            }.joinToString("\n") +
            "\n ----------\n 0 - меню" +
            "\nВаш ответ: "
}

fun main() {
    val trainer = LearnWordTrainer(DICTIONARY_FILE)

    while (true) {
        printMenu()
        print("Выберите пункт меню: ")
        val selectedItem = readln().toInt()
        when (selectedItem) {
            // Учить слова
            1 -> {
                while (true) {
                    println("\nВы выбрали \"Учить слова\"")
                    val question = trainer.getNextQuestion(ANSWER_VARIANTS_COUNT)
                    if (question == null) {
                        println("Все слова в словаре выучены!")
                    } else {
                        println(question.toConsoleString())

                        val userAnswerInput = readln().toInt()
                        if (userAnswerInput == 0) {
                            break
                        } else if (trainer.checkAnswer(userAnswerInput - 1)) {
                            println("Правильно!\n")
                        } else {
                            println("Неправильно! \"${question.correctAnswer.original}\" - это \"${question.correctAnswer.translate}\" \n")
                        }
                    }
                }
            }
            // Статистика
            2 -> {
                val stats = trainer.getStatistics()
                println("\n Статистика: \nВыучено ${stats.learnedCount} из ${stats.totalCount} | ${stats.percent}%)\n")
            }
            // Выход
            0 -> break
        }
    }
}


fun printMenu() {
    println(
        ("""Меню: 
            |1 – Учить слова
            |2 – Статистика
            |0 – Выход""").trimMargin()
    )
}

const val MIN_LEARNED = 3
const val ANSWER_VARIANTS_COUNT = 4
const val DICTIONARY_FILE = "words.txt"