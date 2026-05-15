package com.namma.homestay.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.namma.homestay.data.AppConfig
import com.namma.homestay.data.AppContainer
import com.namma.homestay.ui.di.AppViewModelFactory
import com.namma.homestay.ui.screens.*

@Composable
fun NammaNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val repository = AppContainer.getRepository(context)
    
    // Check if user is already logged in
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    NavHost(
        navController = navController,
        startDestination = Route.RoleSelection.route,
        modifier = Modifier.padding(contentPadding),
    ) {
        composable(Route.RoleSelection.route) {
            RoleSelectionScreen(
                onHostClick = {
                    if (currentUser != null) {
                        navController.navigate(Route.HomeProfile.route) {
                            popUpTo(Route.RoleSelection.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Route.HostAuth.route)
                    }
                },
                onTravelerClick = {
                    navController.navigate(Route.VisitorPreview.route) {
                        popUpTo(Route.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.HostAuth.route) {
            HostAuthScreen(
                onAuthSuccess = { uid ->
                    navController.navigate(Route.HomeProfile.route) {
                        popUpTo(Route.RoleSelection.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Route.HomeProfile.route) {
            val uid = auth.currentUser?.uid ?: "anonymous"
            val factory = AppViewModelFactory(hostId = uid, repository = repository)
            val vm: HomeProfileViewModel = viewModel(factory = factory)
            HomeProfileScreen(vm)
        }

        composable(Route.DailyMenu.route) {
            val uid = auth.currentUser?.uid ?: "anonymous"
            val factory = AppViewModelFactory(hostId = uid, repository = repository)
            val vm: DailyMenuViewModel = viewModel(factory = factory)
            DailyMenuScreen(vm)
        }

        composable(Route.InquiryBox.route) {
            val uid = auth.currentUser?.uid ?: "anonymous"
            val factory = AppViewModelFactory(hostId = uid, repository = repository)
            val vm: InquiryBoxViewModel = viewModel(factory = factory)
            InquiryBoxScreen(vm)
        }

        composable(Route.LocalGuide.route) {
            val uid = auth.currentUser?.uid ?: "anonymous"
            val factory = AppViewModelFactory(hostId = uid, repository = repository)
            val vm: LocalGuideViewModel = viewModel(factory = factory)
            LocalGuideScreen(vm)
        }

        composable(Route.VisitorPreview.route) {
            val factory = AppViewModelFactory(hostId = "", repository = repository)
            val vm: VisitorPreviewViewModel = viewModel(factory = factory)
            VisitorPreviewScreen(
                vm = vm,
                onHomestayClick = { homestayId, hostId ->
                    navController.navigate(Route.HomestayDetail.createRoute(homestayId, hostId))
                }
            )
        }

        composable(
            route = Route.HomestayDetail.route,
            arguments = listOf(
                navArgument("homestayId") { type = NavType.StringType },
                navArgument("hostId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val homestayId = backStackEntry.arguments?.getString("homestayId") ?: ""
            val hostId = backStackEntry.arguments?.getString("hostId") ?: ""
            
            val detailVm: HomestayDetailViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return HomestayDetailViewModel(homestayId, hostId, repository) as T
                    }
                }
            )
            HomestayDetailScreen(vm = detailVm, onBack = { navController.popBackStack() })
        }
    }
}
