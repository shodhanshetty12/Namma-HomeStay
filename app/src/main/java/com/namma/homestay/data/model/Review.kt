package com.namma.homestay.data.model

import java.time.Instant

data class Review(
    val id: String,
    val homestayId: String,
    val userName: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val createdAt: Instant
)
