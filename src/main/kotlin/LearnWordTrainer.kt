package org.example

import kotlinx.serialization.Serializable
import java.io.File

class LearnWordTrainer(
    private val fileName: String = DICTIONARY_FILE,
    private val minLearned: Int = 3,
) {
    val dictionary = loadDictionary(fileName)
    var question: Question? = null

    fun getStatistics(): Statistics {
        val learnedCount = dictionary.count { it.correctAnswersCount >= minLearned }
        val totalCount = dictionary.count()
        val percent: Float = learnedCount.toFloat() / totalCount.toFloat() * 100
        return Statistics(learnedCount, totalCount, percent.toInt())
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

    private fun loadDictionary(fileName: String = this.fileName): List<Word> {
        val wordsFile = File(fileName)
        if (!wordsFile.exists()) {
            File(DICTIONARY_FILE).copyTo(wordsFile, false)
        }
        val dictionary: MutableList<Word> = mutableListOf()
        File(fileName).forEachLine {
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
        File(fileName).writeText(dictionaryText)
    }

    fun resetProgress() {
        File(DICTIONARY_FILE).copyTo(File(this.fileName), true)
        loadDictionary()
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

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

const val MIN_LEARNED = 3
const val ANSWER_VARIANTS_COUNT = 4
const val DICTIONARY_FILE = "words.txt"