package com.namma.homestay.ui.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.namma.homestay.data.repo.NammaRepository
import com.namma.homestay.ui.screens.DailyMenuViewModel
import com.namma.homestay.ui.screens.HomeProfileViewModel
import com.namma.homestay.ui.screens.InquiryBoxViewModel
import com.namma.homestay.ui.screens.LocalGuideViewModel
import com.namma.homestay.ui.screens.VisitorPreviewViewModel

class AppViewModelFactory(
    private val hostId: String,
    private val repository: NammaRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            HomeProfileViewModel::class.java -> HomeProfileViewModel(hostId, repository) as T
            DailyMenuViewModel::class.java -> DailyMenuViewModel(hostId, repository) as T
            InquiryBoxViewModel::class.java -> InquiryBoxViewModel(hostId, repository) as T
            LocalGuideViewModel::class.java -> LocalGuideViewModel(hostId, repository) as T
            VisitorPreviewViewModel::class.java -> VisitorPreviewViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
