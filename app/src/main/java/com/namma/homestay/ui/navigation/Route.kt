package com.namma.homestay.ui.navigation

sealed class Route(
    val route: String,
    val label: String,
) {
    data object RoleSelection : Route("role_selection", "Welcome")
    data object HostAuth : Route("host_auth", "Host Login")
    data object HomeProfile : Route("home_profile", "My Home")
    data object DailyMenu : Route("daily_menu", "Today's Menu")
    data object InquiryBox : Route("inquiry_box", "Inquiries")
    data object LocalGuide : Route("local_guide", "Local Guide")
    data object VisitorPreview : Route("visitor_preview", "Explore")
    data object HomestayDetail : Route("homestay_detail/{homestayId}/{hostId}", "Details") {
        fun createRoute(homestayId: String, hostId: String) = "homestay_detail/$homestayId/$hostId"
    }
}
