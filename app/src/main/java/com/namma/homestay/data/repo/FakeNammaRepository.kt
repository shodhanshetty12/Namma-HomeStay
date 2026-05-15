package com.namma.homestay.data.repo

import android.net.Uri
import com.namma.homestay.data.Result
import com.namma.homestay.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class FakeNammaRepository(
    private val hostId: String,
) : NammaRepository {

    private val profilesFlow = MutableStateFlow(
        listOf(
            HomeProfile(
                id = "fake_01",
                hostId = hostId,
                title = "Malnad Green Stay",
                description = "Experience authentic Malnad culture in our traditional home.",
                dailyRateInr = 1500,
                photoUrls = emptyList(),
                phoneNumber = "9876543210",
                locationUrl = "https://maps.google.com",
                checklist = listOf(
                    VerificationItem("cleanliness", "Cleanliness & Sanitization", true),
                    VerificationItem("toilet", "Toilet Photos Verified", false),
                ),
            )
        )
    )

    private val dailyMenuFlow = MutableStateFlow(
        DailyMenu(
            hostId = hostId,
            date = LocalDate.now(),
            items = listOf("Akki Rotti", "Kaayi Chatney"),
            specialNote = "Vegetables picked this morning.",
            availableRooms = 2,
            dailyRateInr = 1500,
            photoUrl = "",
        ),
    )

    private val inquiriesFlow = MutableStateFlow(emptyList<Inquiry>())
    private val localSpotsFlow = MutableStateFlow(emptyList<LocalSpot>())
    private val reviewsFlow = MutableStateFlow(
        listOf(
            Review(
                id = "rev_01",
                homestayId = "fake_01",
                userName = "Rahul S",
                rating = 5,
                comment = "Amazing hospitality and great food!",
                createdAt = Instant.now()
            )
        )
    )

    override fun observeHomeProfiles(hostId: String): Flow<List<HomeProfile>> = profilesFlow.asStateFlow()

    override suspend fun upsertHomeProfile(profile: HomeProfile): Result<Unit> {
        val list = profilesFlow.value.toMutableList()
        val id = profile.id.ifBlank { UUID.randomUUID().toString() }
        val updatedProfile = profile.copy(id = id)
        
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0) {
            list[index] = updatedProfile
        } else {
            list.add(0, updatedProfile)
        }
        profilesFlow.value = list
        return Result.Success(Unit)
    }

    override suspend fun deleteHomeProfile(hostId: String, profileId: String): Result<Unit> {
        profilesFlow.value = profilesFlow.value.filterNot { it.id == profileId }
        return Result.Success(Unit)
    }

    override suspend fun uploadHomePhoto(hostId: String, uri: Uri): Result<String> {
        return Result.Success(uri.toString())
    }

    // Traveler functions
    override fun observeAllHomeProfiles(): Flow<List<HomeProfile>> = profilesFlow.asStateFlow()

    override fun observeReviews(homestayId: String): Flow<List<Review>> = reviewsFlow.map { reviews ->
        reviews.filter { it.homestayId == homestayId }
    }

    override suspend fun addReview(review: Review): Result<Unit> {
        val list = reviewsFlow.value.toMutableList()
        val id = review.id.ifBlank { UUID.randomUUID().toString() }
        list.add(0, review.copy(id = id))
        reviewsFlow.value = list
        return Result.Success(Unit)
    }

    override fun observeDailyMenu(hostId: String): Flow<DailyMenu> = dailyMenuFlow.asStateFlow()
    override suspend fun updateDailyMenu(menu: DailyMenu): Result<Unit> {
        dailyMenuFlow.value = menu
        return Result.Success(Unit)
    }
    override suspend fun uploadMenuPhoto(hostId: String, uri: Uri): Result<String> {
        val url = uri.toString()
        dailyMenuFlow.value = dailyMenuFlow.value.copy(photoUrl = url)
        return Result.Success(url)
    }

    override fun observeInquiries(hostId: String): Flow<List<Inquiry>> = inquiriesFlow.asStateFlow()
    override suspend fun createInquiry(hostId: String, inquiry: Inquiry): Result<Unit> {
        inquiriesFlow.value = listOf(inquiry) + inquiriesFlow.value
        return Result.Success(Unit)
    }

    override fun observeLocalSpots(hostId: String): Flow<List<LocalSpot>> = localSpotsFlow.asStateFlow()
    override suspend fun upsertLocalSpot(hostId: String, spot: LocalSpot): Result<Unit> {
        val list = localSpotsFlow.value.toMutableList()
        val index = list.indexOfFirst { it.id == spot.id }
        if (index >= 0) list[index] = spot else list.add(0, spot)
        localSpotsFlow.value = list
        return Result.Success(Unit)
    }
    override suspend fun deleteLocalSpot(hostId: String, spotId: String): Result<Unit> {
        localSpotsFlow.value = localSpotsFlow.value.filterNot { it.id == spotId }
        return Result.Success(Unit)
    }

    override suspend fun enhanceDescription(description: String): Result<String> {
        return Result.Success("✨ $description\n\n(AI Enhanced for a better guest experience)")
    }
}
