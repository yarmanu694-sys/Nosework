package com.example.my.models

import java.util.*

data class penaltyRule(
    val name: String = "",
    val type: String = "", // "serial" или "all"
    val points: List<Int> // например: [10, 20, 30]

)
