package models

data class ResultItem(
    val participantId: String,
    val categoryName: String,
    val duration: String,
    val foundMarkers: Int,
    val totalMarkers: Int,
    val penaltyPoints: Int
)