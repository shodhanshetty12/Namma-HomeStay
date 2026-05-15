package com.namma.homestay.data.model

import java.time.LocalDate

data class DailyMenu(
    val hostId: String,
    val date: LocalDate,
    val items: List<String>,
    val specialNote: String,
    val availableRooms: Int,
    val dailyRateInr: Int,
    val photoUrl: String = "",
)
