package com.example.greenalert.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.greenalert.ui.edit.EditDestinationScreen
import com.example.greenalert.ui.home.HomeScreen
import com.example.greenalert.ui.manualinput.ManualInputScreen
import com.example.greenalert.ui.mappicker.MapPickerScreen
import com.example.greenalert.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    sharedAddress: String? = null,
    sharedLat: Double? = null,
    sharedLng: Double? = null
) {
    NavHost(
        navController = navController,
        startDestination = if (sharedAddress != null || sharedLat != null) {
            Screen.ManualInput.createRoute(sharedAddress, sharedLat, sharedLng)
        } else {
            startDestination
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMapPicker = {
                    navController.navigate(Screen.MapPicker.route)
                },
                onNavigateToManualInput = {
                    navController.navigate(Screen.ManualInput.createRoute())
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToEdit = { destinationId ->
                    navController.navigate(Screen.EditDestination.createRoute(destinationId))
                }
            )
        }

        composable(Screen.MapPicker.route) {
            MapPickerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ManualInput.route,
            arguments = listOf(
                navArgument("address") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("lat") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("lng") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address")?.takeIf { it.isNotBlank() }
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()

            ManualInputScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                initialAddress = address,
                initialLat = lat,
                initialLng = lng
            )
        }

        composable(
            route = Screen.EditDestination.route,
            arguments = listOf(
                navArgument("destinationId") {
                    type = NavType.LongType
                }
            )
        ) {
            EditDestinationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
