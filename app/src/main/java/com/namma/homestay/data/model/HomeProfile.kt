package com.namma.homestay.data.model

data class HomeProfile(
    val id: String,
    val hostId: String,
    val title: String,
    val description: String,
    val dailyRateInr: Int,
    val photoUrls: List<String>,
    val checklist: List<VerificationItem>,
    val locationUrl: String = "",
    val phoneNumber: String = "",
)
