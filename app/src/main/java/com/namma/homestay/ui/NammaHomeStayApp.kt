package com.namma.homestay.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.namma.homestay.ui.components.NammaTopAppBar
import com.namma.homestay.ui.navigation.NammaNavGraph
import com.namma.homestay.ui.navigation.Route

@Composable
fun NammaHomeStayApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    // Define which routes belong to which role for the bottom bar
    val hostItems = listOf(
        Route.HomeProfile to Icons.Filled.Home,
        Route.DailyMenu to Icons.Filled.RestaurantMenu,
        Route.InquiryBox to Icons.Filled.Chat,
        Route.LocalGuide to Icons.Filled.Map,
    )
    
    val travelerItems = listOf(
        Route.VisitorPreview to Icons.Filled.TravelExplore,
    )

    val allItems = hostItems + travelerItems

    val isRoleSelection = currentDestination?.route == Route.RoleSelection.route
    val isHostAuth = currentDestination?.route == Route.HostAuth.route
    
    val isHostRoute = hostItems.any { (route, _) -> 
        currentDestination?.hierarchy?.any { it.route == route.route } == true 
    }
    val isTravelerRoute = travelerItems.any { (route, _) -> 
        currentDestination?.hierarchy?.any { it.route == route.route } == true 
    }

    val currentItems = when {
        isHostRoute -> hostItems
        isTravelerRoute -> travelerItems
        else -> emptyList()
    }

    val currentTitle = allItems.firstOrNull { (route, _) ->
        currentDestination?.hierarchy?.any { it.route == route.route } == true
    }?.first?.label ?: "Namma-HomeStay"

    Scaffold(
        topBar = {
            if (!isRoleSelection && !isHostAuth) {
                NammaTopAppBar(
                    title = currentTitle,
                    actions = {
                        if (isHostRoute) {
                            IconButton(onClick = {
                                navController.navigate(Route.VisitorPreview.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                }
                            }) {
                                Icon(Icons.Default.Visibility, "Switch to Traveler View")
                            }
                            IconButton(onClick = {
                                auth.signOut()
                                navController.navigate(Route.RoleSelection.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }) {
                                Icon(Icons.Default.Logout, "Logout")
                            }
                        } else if (isTravelerRoute) {
                            IconButton(onClick = {
                                if (currentUser != null) {
                                    navController.navigate(Route.HomeProfile.route)
                                } else {
                                    navController.navigate(Route.HostAuth.route)
                                }
                            }) {
                                Icon(Icons.Default.HomeRepairService, "Host Dashboard")
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isRoleSelection && !isHostAuth && currentItems.isNotEmpty()) {
                NavigationBar {
                    currentItems.forEach { (route, icon) ->
                        val selected = currentDestination?.hierarchy?.any { it.route == route.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                }
                            },
                            icon = { Icon(imageVector = icon, contentDescription = route.label) },
                            label = { Text(route.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NammaNavGraph(
            navController = navController,
            contentPadding = padding,
        )
    }
}
