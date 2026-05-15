package com.namma.homestay.data.model

import java.time.Instant

data class Inquiry(
    val id: String,
    val fromName: String,
    val message: String,
    val phone: String,
    val createdAt: Instant,
)

