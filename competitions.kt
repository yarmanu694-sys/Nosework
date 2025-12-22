package com.example.my.models

import java.util.*

data class competitions(
    val id: String = "",
    val name: String = "",
    val categories: List<Category> = emptyList(),
    val penaltyRules: List<PenaltyRule> = emptyList()
)