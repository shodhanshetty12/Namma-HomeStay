package com.namma.homestay.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.namma.homestay.data.AppConfig
import com.namma.homestay.data.Result
import com.namma.homestay.data.model.HomeProfile
import com.namma.homestay.data.model.VerificationItem
import com.namma.homestay.data.repo.NammaRepository
import com.namma.homestay.ui.components.FeedbackSnackbar
import com.namma.homestay.ui.components.NammaTextField
import com.namma.homestay.ui.components.PrimaryButtonRow
import com.namma.homestay.ui.components.SectionCard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HomeProfileViewModel(
    private val hostId: String,
    val repository: NammaRepository, // Made public to access AI service
) : ViewModel() {

    var message: String? by mutableStateOf(null)
        private set

    var isUploading by mutableStateOf(false)
        private set
    
    var isEnhancing by mutableStateOf(false)
        private set

    val defaultChecklist = listOf(
        VerificationItem("v1", "Clean Toilets", false),
        VerificationItem("v2", "Fresh Bed Linens", false),
        VerificationItem("v3", "Safe Drinking Water", false),
        VerificationItem("v4", "Surrounding Cleanliness", false)
    )

    val profiles: StateFlow<List<HomeProfile>> =
        repository.observeHomeProfiles(hostId).map { list ->
            list.map { if (it.checklist.isEmpty()) it.copy(checklist = defaultChecklist) else it }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun saveProfile(profile: HomeProfile) {
        viewModelScope.launch {
            when (val res = repository.upsertHomeProfile(profile)) {
                is Result.Success -> message = "Homestay saved successfully!"
                is Result.Error -> message = "Error: ${res.message}"
            }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            repository.deleteHomeProfile(hostId, profileId)
            message = "Homestay removed."
        }
    }

    suspend fun uploadPhoto(uri: Uri): String? {
        isUploading = true
        val res = repository.uploadHomePhoto(hostId, uri)
        isUploading = false
        return if (res is Result.Success) res.value else {
            message = (res as Result.Error).message
            null
        }
    }

    fun enhanceDescription(current: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            isEnhancing = true
            when (val res = repository.enhanceDescription(current)) {
                is Result.Success -> onResult(res.value)
                is Result.Error -> message = res.message
            }
            isEnhancing = false
        }
    }

    fun clearMessage() {
        message = null
    }
}

@Composable
fun HomeProfileScreen(vm: HomeProfileViewModel) {
    val profiles by vm.profiles.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showAddForm by rememberSaveable { mutableStateOf(false) }
    
    // Form state
    var editingId by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var rate by rememberSaveable { mutableStateOf("") }
    var locationUrl by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    
    // Transient state for photos and checklist in the form
    var formPhotos by remember { mutableStateOf(listOf<String>()) }
    var formChecklist by remember { mutableStateOf(vm.defaultChecklist) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val url = vm.uploadPhoto(it)
                if (url != null) formPhotos = formPhotos + url
            }
        }
    }

    val resetForm = {
        editingId = ""
        title = ""
        description = ""
        rate = ""
        locationUrl = ""
        phoneNumber = ""
        formPhotos = emptyList()
        formChecklist = vm.defaultChecklist
        showAddForm = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "My Homestays",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (!showAddForm) {
                item {
                    Button(
                        onClick = { resetForm(); showAddForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Default.AddHome, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add New Homestay Spot", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                item {
                    SectionCard(title = if (editingId.isEmpty()) "NEW HOMESTAY DETAILS" else "EDIT HOMESTAY") {
                        NammaTextField(value = title, onValueChange = { title = it }, label = "Homestay Name")
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            NammaTextField(
                                value = description, 
                                onValueChange = { description = it }, 
                                label = "Description", 
                                minLines = 3,
                                placeholder = "Briefly describe your home..."
                            )
                            // AI MAGIC WAND BUTTON
                            IconButton(
                                onClick = { vm.enhanceDescription(description) { enhanced -> description = enhanced } },
                                modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 24.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                enabled = !vm.isEnhancing
                            ) {
                                if (vm.isEnhancing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, "AI Enhance")
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            NammaTextField(value = rate, onValueChange = { rate = it }, label = "Price/Day", isDigitOnly = true, prefix = { Text("₹ ") }, modifier = Modifier.weight(1f))
                            NammaTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = "Phone", leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) }, modifier = Modifier.weight(1.2f))
                        }
                        
                        NammaTextField(value = locationUrl, onValueChange = { locationUrl = it }, label = "Maps Link (Optional)", leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)) })

                        Divider(Modifier.padding(vertical = 12.dp, horizontal = 20.dp))
                        
                        // FORM PHOTOS
                        Text("Photos", modifier = Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        if (formPhotos.isNotEmpty()) {
                            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(formPhotos) { url ->
                                    Box {
                                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                        IconButton(
                                            onClick = { formPhotos = formPhotos.filter { it != url } },
                                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                        
                        TextButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.padding(horizontal = 16.dp), enabled = !vm.isUploading) {
                            Icon(Icons.Default.AddPhotoAlternate, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (vm.isUploading) "Uploading..." else "Add Photo to this Spot")
                        }

                        // FORM CHECKLIST
                        Divider(Modifier.padding(vertical = 12.dp, horizontal = 20.dp))
                        Text("Verification Checklist", modifier = Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        formChecklist.forEach { item ->
                            Surface(
                                onClick = { formChecklist = formChecklist.map { if (it.id == item.id) it.copy(isDone = !it.isDone) else it } },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                color = Color.Transparent
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                    Icon(
                                        imageVector = if (item.isDone) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                        contentDescription = null,
                                        tint = if (item.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(item.title, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        PrimaryButtonRow(
                            primaryText = "Save Homestay",
                            onPrimaryClick = {
                                if (title.isNotBlank()) {
                                    vm.saveProfile(HomeProfile(
                                        id = if (editingId.isEmpty()) UUID.randomUUID().toString() else editingId,
                                        hostId = AppConfig.hostId,
                                        title = title,
                                        description = description,
                                        dailyRateInr = rate.toIntOrNull() ?: 0,
                                        photoUrls = formPhotos,
                                        checklist = formChecklist,
                                        locationUrl = locationUrl,
                                        phoneNumber = phoneNumber
                                    ))
                                    resetForm()
                                }
                            },
                            secondaryText = "Cancel",
                            onSecondaryClick = { resetForm() },
                            isLoading = vm.isUploading
                        )
                    }
                }
            }

            items(profiles.filter { it.id != editingId }, key = { it.id }) { profile ->
                ProfileListItem(
                    profile = profile,
                    onEdit = {
                        editingId = profile.id
                        title = profile.title
                        description = profile.description
                        rate = profile.dailyRateInr.toString()
                        locationUrl = profile.locationUrl
                        phoneNumber = profile.phoneNumber
                        formPhotos = profile.photoUrls
                        formChecklist = profile.checklist
                        showAddForm = true
                    },
                    onDelete = { vm.deleteProfile(profile.id) }
                )
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
fun ProfileListItem(profile: HomeProfile, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Home, null, modifier = Modifier.padding(10.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("₹${profile.dailyRateInr} / day", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.outline) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error) }
            }

            if (profile.photoUrls.isNotEmpty()) {
                LazyRow(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(profile.photoUrls) { url ->
                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                    }
                }
            }

            val doneCount = profile.checklist.count { it.isDone }
            if (doneCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$doneCount Verified Features", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
