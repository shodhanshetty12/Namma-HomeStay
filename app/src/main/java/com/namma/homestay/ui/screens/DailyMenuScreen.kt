package com.namma.homestay.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.namma.homestay.data.Result
import com.namma.homestay.data.model.DailyMenu
import com.namma.homestay.data.repo.NammaRepository
import com.namma.homestay.ui.components.FeedbackSnackbar
import com.namma.homestay.ui.components.NammaTextField
import com.namma.homestay.ui.components.PrimaryButtonRow
import com.namma.homestay.ui.components.SectionCard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DailyMenuViewModel(
    private val hostId: String,
    private val repository: NammaRepository,
) : ViewModel() {

    var message: String? by mutableStateOf(null)
        private set

    var isUploading by mutableStateOf(false)
        private set

    val menu: StateFlow<DailyMenu> =
        repository.observeDailyMenu(hostId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyMenu(
                hostId = hostId,
                date = LocalDate.now(),
                items = emptyList(),
                specialNote = "",
                availableRooms = 0,
                dailyRateInr = 0,
                photoUrl = "",
            ),
        )

    fun save(updated: DailyMenu) {
        viewModelScope.launch {
            when (val res = repository.updateDailyMenu(updated)) {
                is Result.Success -> message = "Today's menu updated!"
                is Result.Error -> message = res.message
            }
        }
    }

    fun uploadMenuPhoto(uri: Uri) {
        viewModelScope.launch {
            isUploading = true
            when (val res = repository.uploadMenuPhoto(hostId, uri)) {
                is Result.Success -> message = "Menu photo updated!"
                is Result.Error -> message = res.message
            }
            isUploading = false
        }
    }

    fun clearMessage() {
        message = null
    }
}

@Composable
fun DailyMenuScreen(vm: DailyMenuViewModel) {
    val menu by vm.menu.collectAsState()
    val scrollState = rememberScrollState()

    var isEditing by rememberSaveable { mutableStateOf(false) }
    var itemsText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var rooms by rememberSaveable { mutableStateOf("") }
    var rate by rememberSaveable { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.uploadMenuPhoto(it) }
    }

    LaunchedEffect(menu) {
        if (!isEditing) {
            itemsText = menu.items.joinToString(", ")
            note = menu.specialNote
            rooms = menu.availableRooms.toString()
            rate = menu.dailyRateInr.toString()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Today's Menu",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = "Keep travelers updated with what's special today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Menu Photo Section
            SectionCard(title = "Menu Photo") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (menu.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = menu.photoUrl,
                            contentDescription = "Today's Special",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No photo added for today", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    
                    PrimaryButtonRow(
                        primaryText = if (vm.isUploading) "Uploading..." else "Add Food Photo",
                        onPrimaryClick = { photoPicker.launch("image/*") },
                        isLoading = vm.isUploading
                    )
                }
            }

            SectionCard(title = "Food & Availability") {
                NammaTextField(
                    value = itemsText,
                    onValueChange = { itemsText = it; isEditing = true },
                    label = "Dishes (comma separated)",
                    placeholder = "e.g. Akki Rotti, Fish Curry..."
                )
                NammaTextField(
                    value = note,
                    onValueChange = { note = it; isEditing = true },
                    label = "Chef's Special Note",
                    placeholder = "e.g. Made with farm-fresh organic coconut",
                    minLines = 2
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    NammaTextField(
                        value = rooms,
                        onValueChange = { rooms = it; isEditing = true },
                        label = "Rooms Ready",
                        isDigitOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                    NammaTextField(
                        value = rate,
                        onValueChange = { rate = it; isEditing = true },
                        label = "Rate Today",
                        isDigitOnly = true,
                        prefix = { Text("₹ ") },
                        modifier = Modifier.weight(1f)
                    )
                }

                PrimaryButtonRow(
                    primaryText = "Save Today's Menu",
                    onPrimaryClick = {
                        val parsedItems = itemsText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        isEditing = false
                        vm.save(
                            menu.copy(
                                date = LocalDate.now(),
                                items = parsedItems,
                                specialNote = note,
                                availableRooms = rooms.toIntOrNull() ?: 0,
                                dailyRateInr = rate.toIntOrNull() ?: 0,
                            ),
                        )
                    }
                )
            }

            Spacer(Modifier.height(100.dp))
        }

        FeedbackSnackbar(
            message = vm.message,
            onDismiss = { vm.clearMessage() },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}
