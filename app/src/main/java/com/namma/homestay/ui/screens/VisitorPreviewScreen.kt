package com.namma.homestay.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namma.homestay.data.model.*
import com.namma.homestay.data.repo.NammaRepository
import com.namma.homestay.ui.components.HomestayListingCard
import com.namma.homestay.ui.components.ReviewDialog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class VisitorPreviewViewModel(
    private val repository: NammaRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val allProfiles: StateFlow<List<HomeProfile>> = combine(
        repository.observeAllHomeProfiles(),
        _searchQuery
    ) { profiles, query ->
        if (query.isBlank()) profiles
        else profiles.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true) 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var message: String? by mutableStateOf(null)
        private set

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addReview(homestayId: String, userName: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val review = Review(UUID.randomUUID().toString(), homestayId, userName, rating, comment, Instant.now())
            repository.addReview(review)
            message = "Thank you for your feedback!"
        }
    }

    fun getReviews(homestayId: String): Flow<List<Review>> = repository.observeReviews(homestayId)

    fun shareProfile(context: Context, profile: HomeProfile) {
        val shareText = "Namaskara! Check out this homestay: ${profile.title}\nRate: ₹${profile.dailyRateInr}/day\nPhone: ${profile.phoneNumber}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun clearMessage() { message = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorPreviewScreen(
    vm: VisitorPreviewViewModel,
    onHomestayClick: (String, String) -> Unit
) {
    val profiles by vm.allProfiles.collectAsState()
    val query by vm.searchQuery.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text("Find Your Coastal Escape", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or vibe...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { vm.onSearchQueryChange("") }) { Icon(Icons.Default.Clear, null) } },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            if (profiles.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Text("No homestays found for '$query'", modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                items(profiles) { profile ->
                    HomestayTravelerCard(
                        profile = profile,
                        onClick = { onHomestayClick(profile.id, profile.hostId) },
                        onShare = { vm.shareProfile(context, profile) },
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile.phoneNumber}"))
                            context.startActivity(intent)
                        },
                        onAddReview = { name, rating, comment -> vm.addReview(profile.id, name, rating, comment) },
                        reviewsFlow = vm.getReviews(profile.id)
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        com.namma.homestay.ui.components.FeedbackSnackbar(
            message = vm.message,
            onDismiss = { vm.clearMessage() },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
fun HomestayTravelerCard(
    profile: HomeProfile,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onCall: () -> Unit,
    onAddReview: (String, Int, String) -> Unit,
    reviewsFlow: Flow<List<Review>>
) {
    val reviews by reviewsFlow.collectAsState(initial = emptyList())
    var showReviewDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.clickable { onClick() }) {
        HomestayListingCard(
            title = profile.title,
            description = profile.description,
            price = profile.dailyRateInr,
            imageUrl = profile.photoUrls.firstOrNull(),
            verifiedItems = profile.checklist.filter { it.isDone }.map { it.title },
            locationUrl = profile.locationUrl,
            rating = if (reviews.isEmpty()) 5.0 else reviews.map { it.rating }.average(),
            reviewCount = reviews.size,
            onLocationClick = { /* Handled in detail */ }
        )
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("SHARE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onCall, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("CALL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showReviewDialog) {
        ReviewDialog(onDismiss = { showReviewDialog = false }, onSave = onAddReview)
    }
}
