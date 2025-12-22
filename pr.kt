package com.example.my.models

import java.util.*

data class pr(
    var participantId: String = "",
    var competitionId: String = "",
    var startTime: Long = 0L,
    var finishTime: Long? = null,
    var markerDetectionTimes: MutableList<Long> = mutableListOf(),
    var penalties: MutableList<PenaltyInstance> = mutableListOf(),
    var categoryName: String = ""
)