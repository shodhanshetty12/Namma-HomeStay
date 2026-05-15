package com.namma.homestay.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.namma.homestay.data.model.*
import com.namma.homestay.data.repo.NammaRepository
import com.namma.homestay.ui.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class HomestayDetailViewModel(
    private val homestayId: String,
    private val hostId: String,
    private val repository: NammaRepository
) : ViewModel() {

    val uiState: StateFlow<HomestayDetailUiState> = combine(
        repository.observeAllHomeProfiles().map { list -> list.find { it.id == homestayId } },
        repository.observeDailyMenu(hostId),
        repository.observeLocalSpots(hostId),
        repository.observeReviews(homestayId)
    ) { profile, menu, spots, reviews ->
        HomestayDetailUiState(profile, menu, spots, reviews)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomestayDetailUiState())

    var message: String? by mutableStateOf(null)
        private set

    fun addReview(userName: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val review = Review(UUID.randomUUID().toString(), homestayId, userName, rating, comment, Instant.now())
            repository.addReview(review)
            message = "Review posted!"
        }
    }

    fun sendInquiry(name: String, guestMessage: String, phone: String) {
        viewModelScope.launch {
            val inquiry = Inquiry(UUID.randomUUID().toString(), name, guestMessage, phone, Instant.now())
            repository.createInquiry(hostId, inquiry)
            message = "Message sent to host!"
        }
    }

    fun clearMessage() { message = null }
}

data class HomestayDetailUiState(
    val profile: HomeProfile? = null,
    val menu: DailyMenu? = null,
    val spots: List<LocalSpot> = emptyList(),
    val reviews: List<Review> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomestayDetailScreen(vm: HomestayDetailViewModel, onBack: () -> Unit) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    var showReviewDialog by remember { mutableStateOf(false) }
    var showInquiryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.profile?.title ?: "Homestay Details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            val profile = state.profile
            if (profile == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (profile.photoUrls.isNotEmpty()) {
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(profile.photoUrls) { url ->
                                    Card(shape = RoundedCornerShape(16.dp)) {
                                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(width = 320.dp, height = 220.dp), contentScale = ContentScale.Crop)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionCard(title = "Experience the Local Life") {
                            Text(profile.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
                            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { showInquiryDialog = true }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Send Message")
                                }
                                OutlinedButton(onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile.phoneNumber}"))
                                    context.startActivity(intent)
                                }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Call, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Call Host")
                                }
                            }
                        }
                    }

                    state.menu?.let { menu ->
                        if (menu.items.isNotEmpty()) {
                            item {
                                SectionCard(title = "TODAY'S SPECIAL MENU") {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(menu.items.joinToString(" • "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                        if (menu.specialNote.isNotBlank()) Text(menu.specialNote, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showReviewDialog = true }) { Text("Rate this Stay") }
                        }
                    }
                    items(state.reviews) { review ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(review.userName, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.weight(1f))
                                    Text("⭐".repeat(review.rating))
                                }
                                Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }

            FeedbackSnackbar(message = vm.message, onDismiss = { vm.clearMessage() }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp))
        }
    }

    if (showReviewDialog) ReviewDialog(onDismiss = { showReviewDialog = false }, onSave = { n, r, c -> vm.addReview(n, r, c); showReviewDialog = false })
    if (showInquiryDialog) InquiryDialog(onDismiss = { showInquiryDialog = false }, onSend = { n, m, p -> vm.sendInquiry(n, m, p); showInquiryDialog = false })
}
