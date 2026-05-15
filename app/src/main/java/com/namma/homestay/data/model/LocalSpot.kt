package com.namma.homestay.data.model

data class LocalSpot(
    val id: String,
    val name: String,
    val description: String,
    val mapsUrl: String,
    // Used to show newest spots first; default keeps older data compatible.
    val createdAt: Long = 0L,
)
