package com.namma.homestay.data

/**
 * Quick switches for running the app in Android Studio.
 *
 * - Keep [useFakeBackend] = true until you add Firebase (google-services.json).
 * - Set [hostId] to a stable id for each host (or your auth user id later).
 */
// C:/Users/123/Desktop/Namma-HomeStay/app/src/main/java/com/namma/homestay/data/AppConfig.kt

object AppConfig {
    const val hostId: String = "my_real_homestay_01" // Change this to any unique ID you like
    const val useFakeBackend: Boolean = false       // <--- CHANGE THIS TO FALSE
}
