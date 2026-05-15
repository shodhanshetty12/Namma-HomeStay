package com.namma.homestay.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.namma.homestay.data.Result
import com.namma.homestay.data.model.LocalSpot
import com.namma.homestay.data.repo.NammaRepository
import com.namma.homestay.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class LocalGuideViewModel(
    private val hostId: String,
    private val repository: NammaRepository,
) : ViewModel() {

    var message: String? by mutableStateOf(null)
        private set

    val spots: StateFlow<List<LocalSpot>> =
        repository.observeLocalSpots(hostId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun addSpot(name: String, description: String, mapsUrl: String) {
        val spot = LocalSpot(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            mapsUrl = mapsUrl,
            createdAt = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            when (val res = repository.upsertLocalSpot(hostId, spot)) {
                is Result.Success -> message = "New spot added: ${spot.name}"
                is Result.Error -> message = "Error: ${res.message}"
            }
        }
    }

    fun deleteSpot(id: String) {
        viewModelScope.launch {
            repository.deleteLocalSpot(hostId, id)
            message = "Spot removed"
        }
    }

    fun clearMessage() {
        message = null
    }
}

@Composable
fun LocalGuideScreen(vm: LocalGuideViewModel) {
    val spots by vm.spots.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var maps by remember { mutableStateOf("") }

    // Newest first
    val sortedSpots = remember(spots) { spots.sortedByDescending { it.createdAt } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Local Hidden Gems",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Share nearby waterfalls, viewpoints, or secret spots with your guests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
            }

            item {
                SectionCard(title = "Add New Spot") {
                    NammaTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Spot Name",
                        placeholder = "e.g. Secret Waterfall",
                        leadingIcon = { Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    NammaTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = "How to reach?",
                        placeholder = "e.g. 10 min walk through the areca farm",
                        minLines = 2
                    )
                    NammaTextField(
                        value = maps,
                        onValueChange = { maps = it },
                        label = "Google Maps Link (Optional)",
                        placeholder = "Paste the link here",
                        leadingIcon = { Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    PrimaryButtonRow(
                        primaryText = "Add Spot",
                        onPrimaryClick = {
                            if (name.isNotBlank()) {
                                vm.addSpot(name.trim(), desc.trim(), maps.trim())
                                name = ""
                                desc = ""
                                maps = ""
                            }
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Existing Spots (${spots.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (sortedSpots.isEmpty()) {
                item {
                    Text(
                        "You haven't added any spots yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(sortedSpots, key = { it.id }) { spot ->
                    SpotListItem(
                        spot = spot,
                        onDelete = { vm.deleteSpot(spot.id) },
                        onViewOnMap = {
                            val uri = if (spot.mapsUrl.isNotBlank()) {
                                Uri.parse(spot.mapsUrl)
                            } else {
                                Uri.parse("geo:0,0?q=${Uri.encode(spot.name)}")
                            }
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        FeedbackSnackbar(
            message = vm.message,
            onDismiss = { vm.clearMessage() },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun SpotListItem(
    spot: LocalSpot,
    onDelete: () -> Unit,
    onViewOnMap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        spot.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (spot.description.isNotBlank()) {
                        Text(
                            spot.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete")
                }
            }

            if (spot.mapsUrl.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                OutlinedButton(
                    onClick = onViewOnMap,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Navigate to Spot", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
