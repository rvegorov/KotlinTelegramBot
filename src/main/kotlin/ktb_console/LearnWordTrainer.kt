package org.example.ktb_console

import java.io.File

class LearnWordTrainer(private val dictionaryFile: String, private val minLearned: Int) {
    val dictionary = loadDictionary(dictionaryFile)
    private var question: Question? = null

    fun getStatistics(): Statistics {
        val learnedCount = dictionary.count { it.correctAnswersCount >= minLearned }
        val totalCount = dictionary.count()
        val percent = learnedCount / totalCount * 100
        return Statistics(learnedCount, totalCount, percent)
    }

    fun getNextQuestion(maxVariants: Int): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < minLearned }

        if (notLearnedList.isEmpty()) {
            return null
        }
        val questionWords = if (notLearnedList.size < maxVariants) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= minLearned }
            (notLearnedList + learnedList.take(maxVariants - notLearnedList.size)).shuffled()
        } else {
            notLearnedList.shuffled().take(maxVariants)
        }
        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userInputId: Int?): Boolean {

        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (userInputId == correctAnswerId) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(dictionaryFile: String = this.dictionaryFile): List<Word> {

        val dictionary: MutableList<Word> = mutableListOf()
        File(dictionaryFile).forEachLine {
            val line = it.split("|")
            val word = Word(line[0], line[1], line[2].toIntOrNull() ?: 0)
            dictionary.add(word)
        }
        return dictionary
    }

    private fun saveDictionary() {
        val dictionaryText = dictionary.joinToString("\n") { word ->
            "${word.original}|${word.translate}|${word.correctAnswersCount}"
        }
        File(dictionaryFile).writeText(dictionaryText)
    }
}

data class Statistics(
    val learnedCount: Int,
    val totalCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)