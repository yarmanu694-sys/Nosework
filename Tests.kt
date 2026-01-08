package utils

import models.*
import org.junit.Test
import org.junit.Assert.assertEquals

class CompetitionSessionTest {

    @Test
    fun `serial penalty should increase correctly`() {
        val competition = competitions(
            id = "test",
            categories = listOf(
                category("test", totalMarkers = 10, totalTimeSeconds = 60, totalPoints = 50)
            ),
            penaltyRules = listOf(
                penaltyRule("false", "serial", listOf(5, 10, 15))
            )
        )

        val session = CompetitionSession(competition)
        session.startSession("testUser", "test")

        session.applyPenalty("false")
        session.applyPenalty("false")

        val total = session.getCurrentResult()?.penalties?.sumOf { it.points } ?: 0
        assertEquals(15, total) // 5 + 10
    }
}