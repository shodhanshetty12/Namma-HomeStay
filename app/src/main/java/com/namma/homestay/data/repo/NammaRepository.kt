package com.namma.homestay.data.repo

import android.net.Uri
import com.namma.homestay.data.Result
import com.namma.homestay.data.model.*
import kotlinx.coroutines.flow.Flow

interface NammaRepository {
    // For Hosts
    fun observeHomeProfiles(hostId: String): Flow<List<HomeProfile>>
    suspend fun upsertHomeProfile(profile: HomeProfile): Result<Unit>
    suspend fun deleteHomeProfile(hostId: String, profileId: String): Result<Unit>
    suspend fun uploadHomePhoto(hostId: String, uri: Uri): Result<String>

    // For Travelers
    fun observeAllHomeProfiles(): Flow<List<HomeProfile>>
    fun observeReviews(homestayId: String): Flow<List<Review>>
    suspend fun addReview(review: Review): Result<Unit>

    // Menu
    fun observeDailyMenu(hostId: String): Flow<DailyMenu>
    suspend fun updateDailyMenu(menu: DailyMenu): Result<Unit>
    suspend fun uploadMenuPhoto(hostId: String, uri: Uri): Result<String>

    // Inquiries
    fun observeInquiries(hostId: String): Flow<List<Inquiry>>
    suspend fun createInquiry(hostId: String, inquiry: Inquiry): Result<Unit>

    // Local Spots
    fun observeLocalSpots(hostId: String): Flow<List<LocalSpot>>
    suspend fun upsertLocalSpot(hostId: String, spot: LocalSpot): Result<Unit>
    suspend fun deleteLocalSpot(hostId: String, spotId: String): Result<Unit>
    
    // AI Services (Mocked for now)
    suspend fun enhanceDescription(description: String): Result<String>
}
