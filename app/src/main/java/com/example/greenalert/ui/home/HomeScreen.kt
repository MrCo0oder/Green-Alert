package com.example.greenalert.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.greenalert.ui.components.AddDestinationSheet
import com.example.greenalert.ui.components.DestinationCard
import com.example.greenalert.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMapPicker: () -> Unit,
    onNavigateToManualInput: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddSheet by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showBackgroundPermissionDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }

    // Step 3: Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // All permissions done, enable tracking
        viewModel.toggleTracking(true)
    }

    // Step 2: Background location permission launcher (Android 10+)
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // After background location, request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionUtils.hasNotificationPermission(context)) {
                showNotificationPermissionDialog = true
            } else {
                viewModel.toggleTracking(true)
            }
        } else {
            viewModel.toggleTracking(true)
        }
    }

    // Step 1: Foreground location permission launcher
    val foregroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions.values.all { it }
        if (locationGranted) {
            // After foreground location, request background location
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!PermissionUtils.hasBackgroundLocationPermission(context)) {
                    showBackgroundPermissionDialog = true
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!PermissionUtils.hasNotificationPermission(context)) {
                        showNotificationPermissionDialog = true
                    } else {
                        viewModel.toggleTracking(true)
                    }
                } else {
                    viewModel.toggleTracking(true)
                }
            } else {
                viewModel.toggleTracking(true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Green Alert")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Destination") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tracking Toggle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.preferences.trackingEnabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (uiState.preferences.trackingEnabled) {
                                "🟢 Tracking Active"
                            } else {
                                "⚪ Tracking Disabled"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (uiState.preferences.trackingEnabled) {
                                "You'll be alerted when arriving at destinations"
                            } else {
                                "Enable to receive arrival alerts"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.preferences.trackingEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (PermissionUtils.hasAllRequiredPermissions(context)) {
                                    viewModel.toggleTracking(true)
                                } else {
                                    showPermissionDialog = true
                                }
                            } else {
                                viewModel.toggleTracking(false)
                            }
                        }
                    )
                }
            }

            // Destinations List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.destinations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📍",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No destinations yet",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Add your first destination to start getting arrival alerts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.destinations,
                        key = { it.id }
                    ) { destination ->
                        DestinationCard(
                            destination = destination,
                            onToggleActive = { isActive ->
                                viewModel.toggleDestinationActive(destination, isActive)
                            },
                            onDelete = {
                                viewModel.deleteDestination(destination)
                            }
                        )
                    }

                    // Bottom spacing for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add Destination Bottom Sheet
    if (showAddSheet) {
        AddDestinationSheet(
            onDismiss = { showAddSheet = false },
            onPickFromMap = onNavigateToMapPicker,
            onEnterManually = onNavigateToManualInput
        )
    }

    // Step 1: Initial Permission Dialog (Location)
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Location Permission Required") },
            text = {
                Text("GreenAlert needs location access to detect when you arrive at your destinations.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        foregroundLocationLauncher.launch(PermissionUtils.locationPermissions)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Step 2: Background Location Dialog (Android 10+)
    if (showBackgroundPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showBackgroundPermissionDialog = false },
            title = { Text("Background Location") },
            text = {
                Text("To alert you when you arrive at destinations even when the app is closed, please select \"Allow all the time\" for location access.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackgroundPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showBackgroundPermissionDialog = false
                    // Skip to notification permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        showNotificationPermissionDialog = true
                    } else {
                        viewModel.toggleTracking(true)
                    }
                }) {
                    Text("Skip")
                }
            }
        )
    }

    // Step 3: Notification Permission Dialog (Android 13+)
    if (showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationPermissionDialog = false },
            title = { Text("Notification Permission") },
            text = {
                Text("Allow notifications so GreenAlert can alert you when you arrive at your destinations.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ) {
                    Text("Allow Notifications")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showNotificationPermissionDialog = false
                    viewModel.toggleTracking(true)
                }) {
                    Text("Skip")
                }
            }
        )
    }
}
