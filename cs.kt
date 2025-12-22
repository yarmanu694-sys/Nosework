package com.example.my.logic

import com.example.my.models.*
import java.util.*

class CompetitionSession(private val competition: competitions) {

    private var currentResult: pr? = null
    private var sessionStartTime: Long = 0L
    private var currentCorrectDetections = 0
    private var currentTotalPenaltyPoints = 0
    private val serialPenaltyCounters = mutableMapOf<String, Int>()

    fun startSession(participantId: String, categoryName: String) {
        val category = findCategoryByName(categoryName)
            ?: throw IllegalArgumentException("Категория не найдена: $categoryName")

        currentResult = pr().apply {
            this.participantId = participantId
            this.competitionId = competition.id
            this.categoryName = categoryName
            this.startTime = System.currentTimeMillis()
            this.markerDetectionTimes = mutableListOf()
            this.penalties = mutableListOf()
        }

        sessionStartTime = System.currentTimeMillis()
        currentCorrectDetections = 0
        currentTotalPenaltyPoints = 0
        serialPenaltyCounters.clear()

        println("Сессия начата для участника: $participantId, категория: $categoryName")
    }

    fun registerMarkerDetection(isCorrect: Boolean): Boolean {
        val result = currentResult ?: run {
            println("Сессия не начата!")
            return false
        }

        val category = findCategoryByName(result.categoryName)
            ?: return false

        if (isCorrect) {
            currentCorrectDetections++
            result.markerDetectionTimes.add(System.currentTimeMillis())
            println("Правильное обнаружение! Найдено: $currentCorrectDetections/${category.totalMarkers}")
        } else {
            // Применить штраф за ложное обнаружение
            applyPenalty("false_detection") // Предполагаем, что такое правило существует
        }

        // Проверить условия окончания
        return checkEndConditions(category)
    }

    fun applyPenalty(penaltyRuleName: String) {
        val result = currentResult ?: run {
            println("Сессия не начата!")
            return
        }

        val rule = findPenaltyRuleByName(penaltyRuleName)
            ?: run {
                println("Правило штрафа не найдено: $penaltyRuleName")
                return
            }

        var pointsToApply = 0
        when (rule.type) {
            "serial" -> {
                val counter = serialPenaltyCounters.getOrDefault(penaltyRuleName, 0)
                if (counter < rule.points.size) {
                    pointsToApply = rule.points[counter]
                    serialPenaltyCounters[penaltyRuleName] = counter + 1
                }
            }
            "all" -> {
                // Тип "all" - судья сам решает. Здесь заглушка.
                pointsToApply = 0
            }
        }

        currentTotalPenaltyPoints += pointsToApply
        result.penalties.add(pi(penaltyRuleName, pointsToApply, System.currentTimeMillis()))
        println("Применен штраф: $penaltyRuleName, баллов: $pointsToApply, всего штрафов: $currentTotalPenaltyPoints")
    }

    fun applyCustomPenalty(ruleName: String, pointsToApply: Int) {
        val result = currentResult ?: run {
            println("Сессия не начата!")
            return
        }

        currentTotalPenaltyPoints += pointsToApply
        result.penalties.add(pi(ruleName, pointsToApply, System.currentTimeMillis()))
        println("Применен кастомный штраф: $ruleName, баллов: $pointsToApply, всего штрафов: $currentTotalPenaltyPoints")

        // Проверить условия окончания, если это возможно
        if (result.finishTime == null) {
            val category = findCategoryByName(result.categoryName)
            if (category != null) {
                checkEndConditions(category)
            }
        }
    }

    private fun checkEndConditions(category: category): Boolean {
        val elapsed = System.currentTimeMillis() - sessionStartTime
// Условие 1: Время истекло
if (elapsed >= category.totalTimeSeconds * 1000L) {
    println("Время истекло!")
    endSession()
    return true
}

// Условие 2: Все закладки найдены
if (currentCorrectDetections >= category.totalMarkers) {
    println("Все закладки найдены!")
    endSession()
    return true
}

// Условие 3: Суммарный штраф превысил лимит баллов
if (currentTotalPenaltyPoints >= category.totalPoints) {
    println("Штрафы превысили лимит баллов!")
    endSession()
    return true
}

return false // Сессия продолжается
}

private fun endSession() {
    currentResult?.let { result ->
        if (result.finishTime == null) {
            result.finishTime = System.currentTimeMillis()
            println("Сессия завершена.")
            // Здесь можно сохранить результат в файл или передать в другую часть приложения
        }
    }
}

fun getCurrentResult(): pr? {
    return currentResult
}

private fun findCategoryByName(name: String): category? {
    return competition.categories.find { it.name == name }
}

private fun findPenaltyRuleByName(name: String): pr? {
    return competition.pr.find { it.name == name }
}
}