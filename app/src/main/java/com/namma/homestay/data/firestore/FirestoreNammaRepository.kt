package com.namma.homestay.data.firestore

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.namma.homestay.BuildConfig
import com.namma.homestay.data.Result
import com.namma.homestay.data.model.*
import com.namma.homestay.data.repo.NammaRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.coroutines.resume

class FirestoreNammaRepository(
    context: Context,
    private val db: FirebaseFirestore = Firebase.firestore,
) : NammaRepository {

    // Initialize Gemini AI using the safe API Key from local.properties
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    init {
        try {
            val config = mapOf(
                "cloud_name" to "dlfwxel7f",
                "api_key" to "452916721213542",
                "api_secret" to "GiAjf5TkDi6e0gGmTU20-HXwHzI"
            )
            MediaManager.init(context, config)
        } catch (ignored: Exception) {}
    }

    private fun hostRef(hostId: String) = db.collection(FirestoreSchema.HOSTS).document(hostId)

    private suspend fun uploadToCloudinary(uri: Uri, folder: String): Result<String> = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    if (url != null) continuation.resume(Result.Success(url))
                    else continuation.resume(Result.Error("Upload failed: No URL returned"))
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(Result.Error("Upload failed: ${error?.description}"))
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    override fun observeHomeProfiles(hostId: String): Flow<List<HomeProfile>> = callbackFlow {
        val reg = hostRef(hostId)
            .collection(FirestoreSchema.PROFILE)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().map { doc ->
                    doc.data.orEmpty().toHomeProfile(doc.id, hostId)
                }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override fun observeAllHomeProfiles(): Flow<List<HomeProfile>> = callbackFlow {
        val reg = db.collectionGroup(FirestoreSchema.PROFILE)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().map { doc ->
                    val hId = doc.reference.parent.parent?.id ?: "unknown"
                    doc.data.orEmpty().toHomeProfile(doc.id, hId)
                }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun upsertHomeProfile(profile: HomeProfile): Result<Unit> = try {
        val id = profile.id.ifBlank { UUID.randomUUID().toString() }
        hostRef(profile.hostId)
            .collection(FirestoreSchema.PROFILE)
            .document(id)
            .set(profile.copy(id = id).toMap())
            .await()
        Result.Success(Unit)
    } catch (t: Throwable) {
        Result.Error("Failed to save profile", t)
    }

    override suspend fun deleteHomeProfile(hostId: String, profileId: String): Result<Unit> = try {
        hostRef(hostId)
            .collection(FirestoreSchema.PROFILE)
            .document(profileId)
            .delete()
            .await()
        Result.Success(Unit)
    } catch (t: Throwable) {
        Result.Error("Failed to delete profile", t)
    }

    override suspend fun uploadHomePhoto(hostId: String, uri: Uri): Result<String> {
        return uploadToCloudinary(uri, "namma_homestay/hosts/$hostId")
    }

    override fun observeReviews(homestayId: String): Flow<List<Review>> = callbackFlow {
        val reg = db.collection("reviews")
            .whereEqualTo("homestayId", homestayId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().map { doc ->
                    val data = doc.data.orEmpty()
                    Review(
                        id = doc.id,
                        homestayId = data["homestayId"] as? String ?: "",
                        userName = data["userName"] as? String ?: "Guest",
                        rating = (data["rating"] as? Number)?.toInt() ?: 5,
                        comment = data["comment"] as? String ?: "",
                        createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.toInstant() ?: Instant.now()
                    )
                }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun addReview(review: Review): Result<Unit> = try {
        db.collection("reviews")
            .add(mapOf(
                "homestayId" to review.homestayId,
                "userName" to review.userName,
                "rating" to review.rating,
                "comment" to review.comment,
                "createdAt" to Timestamp(java.util.Date.from(review.createdAt))
            ))
            .await()
        Result.Success(Unit)
    } catch (t: Throwable) {
        Result.Error("Failed to add review", t)
    }

    override suspend fun enhanceDescription(description: String): Result<String> = try {
        val prompt = "Rewrite this homestay description professionally and poetic: $description"
        val response = generativeModel.generateContent(prompt)
        Result.Success(response.text ?: description)
    } catch (e: Exception) {
        Result.Success("✨ Welcome to our peaceful coastal home! $description")
    }

    override fun observeDailyMenu(hostId: String): Flow<DailyMenu> = callbackFlow {
        val reg = hostRef(hostId)
            .collection(FirestoreSchema.MENU)
            .document(FirestoreSchema.MENU_DOC)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val data = snap?.data.orEmpty()
                trySend(data.toDailyMenu(hostId))
            }
        awaitClose { reg.remove() }
    }

    override suspend fun updateDailyMenu(menu: DailyMenu): Result<Unit> = try {
        hostRef(menu.hostId)
            .collection(FirestoreSchema.MENU)
            .document(FirestoreSchema.MENU_DOC)
            .set(menu.toMap())
            .await()
        Result.Success(Unit)
    } catch (t: Throwable) {
        Result.Error("Failed to update daily menu", t)
    }

    override suspend fun uploadMenuPhoto(hostId: String, uri: Uri): Result<String> {
        val uploadRes = uploadToCloudinary(uri, "namma_homestay/menu/$hostId")
        if (uploadRes is Result.Error) return uploadRes
        val url = (uploadRes as Result.Success<String>).value
        return try {
            hostRef(hostId)
                .collection(FirestoreSchema.MENU)
                .document(FirestoreSchema.MENU_DOC)
                .set(mapOf("photoUrl" to url), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Result.Success(url)
        } catch (t: Throwable) {
            Result.Error("Failed to update Firestore menu photo", t)
        }
    }

    override fun observeInquiries(hostId: String): Flow<List<Inquiry>> = callbackFlow {
        val reg = hostRef(hostId)
            .collection(FirestoreSchema.INQUIRIES)
            .orderBy("createdAt")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().map { doc ->
                    val data = doc.data.orEmpty()
                    data.toInquiry(doc.id)
                }
                trySend(list.reversed())
            }
        awaitClose { reg.remove() }
    }

    override suspend fun createInquiry(hostId: String, inquiry: Inquiry): Result<Unit> = try {
        hostRef(hostId)
            .collection(FirestoreSchema.INQUIRIES)
            .add(inquiry.toMap())
            .await()
        Result.Success(Unit)
    } catch (t: Throwable) {
        Result.Error("Failed to create inquiry", t)
    }

    override fun observeLocalSpots(hostId: String): Flow<List<LocalSpot>> = callbackFlow {
        val reg = hostRef(hostId)
            .collection(FirestoreSchema.LOCAL_SPOTS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().map { doc ->
                    val data = doc.data.orEmpty()
                    data.toLocalSpot(doc.id)
                }.sortedByDescending { it.createdAt }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun upsertLocalSpot(hostId: String, spot: LocalSpot): Result<Unit> = try {
        hostRef(hostId)
            .collection(FirestoreSchema.LOCAL_SPOTS)
            .document(spot.id)
            .set(spot.toMap())
            .await()
        Result.Success(Unit)
    } catch (t: Throwable) {
        Result.Error("Failed to save local spot", t)
    }

    override suspend fun deleteLocalSpot(hostId: String, spotId: String): Result<Unit> = try {
        hostRef(hostId)
            .collection(FirestoreSchema.LOCAL_SPOTS)
            .document(spotId)
            .delete()
            .await()
        Result.Success(Unit)
    } catch (t: Throwable) {
        Result.Error("Failed to delete local spot", t)
    }

    private fun Map<String, Any>.toHomeProfile(id: String, hostId: String): HomeProfile {
        return HomeProfile(
            id = id,
            hostId = hostId,
            title = this["title"] as? String ?: "",
            description = this["description"] as? String ?: "",
            dailyRateInr = (this["dailyRateInr"] as? Number)?.toInt() ?: 0,
            photoUrls = (this["photoUrls"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
            locationUrl = this["locationUrl"] as? String ?: "",
            phoneNumber = this["phoneNumber"] as? String ?: "",
            checklist = (this["checklist"] as? List<*>)?.mapNotNull { raw ->
                val m = raw as? Map<*, *> ?: return@mapNotNull null
                VerificationItem(m["id"] as? String ?: "", m["title"] as? String ?: "", m["isDone"] as? Boolean ?: false)
            }.orEmpty()
        )
    }

    private fun HomeProfile.toMap(): Map<String, Any> = mapOf(
        "title" to title,
        "description" to description,
        "dailyRateInr" to dailyRateInr,
        "photoUrls" to photoUrls,
        "locationUrl" to locationUrl,
        "phoneNumber" to phoneNumber,
        "checklist" to checklist.map { mapOf("id" to it.id, "title" to it.title, "isDone" to it.isDone) }
    )

    private fun Map<String, Any>.toDailyMenu(hostId: String): DailyMenu {
        val dateStr = this["date"] as? String
        return DailyMenu(
            hostId = hostId,
            date = dateStr?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now(),
            items = (this["items"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
            specialNote = this["specialNote"] as? String ?: "",
            availableRooms = (this["availableRooms"] as? Number)?.toInt() ?: 0,
            dailyRateInr = (this["dailyRateInr"] as? Number)?.toInt() ?: 0,
            photoUrl = this["photoUrl"] as? String ?: "",
        )
    }

    private fun DailyMenu.toMap(): Map<String, Any> = mapOf(
        "date" to date.toString(),
        "items" to items,
        "specialNote" to specialNote,
        "availableRooms" to availableRooms,
        "dailyRateInr" to dailyRateInr,
        "photoUrl" to photoUrl,
    )

    private fun Map<String, Any>.toInquiry(id: String): Inquiry {
        return Inquiry(
            id = id,
            fromName = this["fromName"] as? String ?: "Traveler",
            message = this["message"] as? String ?: "",
            phone = this["phone"] as? String ?: "",
            createdAt = (this["createdAt"] as? Timestamp)?.toDate()?.toInstant() ?: Instant.now()
        )
    }

    private fun Inquiry.toMap(): Map<String, Any> = mapOf(
        "fromName" to fromName,
        "message" to message,
        "phone" to phone,
        "createdAt" to Timestamp(java.util.Date.from(createdAt)),
    )

    private fun Map<String, Any>.toLocalSpot(id: String): LocalSpot {
        return LocalSpot(
            id = id,
            name = this["name"] as? String ?: "",
            description = this["description"] as? String ?: "",
            mapsUrl = this["mapsUrl"] as? String ?: "",
            createdAt = (this["createdAt"] as? Number)?.toLong() ?: 0L
        )
    }

    private fun LocalSpot.toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "mapsUrl" to mapsUrl,
        "createdAt" to (if (createdAt == 0L) System.currentTimeMillis() else createdAt),
    )
}
