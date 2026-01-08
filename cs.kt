package utils

import models.*
import models.category
import models.competitions
import models.penaltyRule
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
            applyPenalty("false_detection")
        }

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
                } else {
                    // Опционально: игнорировать или использовать последнее значение
                    pointsToApply = rule.points.lastOrNull() ?: 0
                }
            }
            "all" -> {
                // Судейский штраф — не применяется автоматически
                pointsToApply = 0
            }
            else -> {
                println("Неизвестный тип правила штрафа: ${rule.type}")
                return
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

        if (result.finishTime == null) {
            val category = findCategoryByName(result.categoryName)
            if (category != null) {
                checkEndConditions(category)
            }
        }
    }

    private fun checkEndConditions(category: category): Boolean {
        val elapsed = System.currentTimeMillis() - sessionStartTime

        if (elapsed >= category.totalTimeSeconds * 1000L) {
            println("Время истекло!")
            endSession()
            return true
        }

        if (currentCorrectDetections >= category.totalMarkers) {
            println("Все закладки найдены!")
            endSession()
            return true
        }

        if (currentTotalPenaltyPoints >= category.totalPoints) {
            println("Штрафы превысили лимит баллов!")
            endSession()
            return true
        }

        return false
    }

    private fun endSession() {
        currentResult?.let { result ->
            if (result.finishTime == null) {
                result.finishTime = System.currentTimeMillis()
                println("Сессия завершена.")
                // Сохранение результата можно добавить здесь
            }
        }
    }

    fun getCurrentResult(): pr? = currentResult

    private fun findCategoryByName(name: String): category? {
        return competition.categories.find { it.name == name }
    }

    // ✅ Исправлено: возвращает PenaltyRule, а не pr
    private fun findPenaltyRuleByName(name: String): penaltyRule? {
        return competition.penaltyRules.find { it.name == name }
    }
}
