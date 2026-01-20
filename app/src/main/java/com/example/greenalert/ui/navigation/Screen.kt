package com.example.greenalert.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object MapPicker : Screen("map_picker")
    data object ManualInput : Screen("manual_input?address={address}&lat={lat}&lng={lng}") {
        fun createRoute(address: String? = null, lat: Double? = null, lng: Double? = null): String {
            return "manual_input?address=${address ?: ""}&lat=${lat ?: ""}&lng=${lng ?: ""}"
        }
    }
    data object Settings : Screen("settings")
}
