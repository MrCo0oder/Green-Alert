package com.example.greenalert.ui.mappicker

import android.Manifest
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.greenalert.R
import com.example.greenalert.ui.components.RadiusSlider
import com.example.greenalert.util.PermissionUtils
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    onNavigateBack: () -> Unit,
    viewModel: MapPickerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }

    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Navigate back after save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.selectedLatitude != null) {
                        IconButton(onClick = { showBottomSheet = true }) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // OpenStreetMap View
            OSMMapView(
                context = context,
                selectedLatitude = uiState.selectedLatitude,
                selectedLongitude = uiState.selectedLongitude,
                radiusMeters = uiState.radiusMeters,
                onLocationSelected = { lat, lng ->
                    viewModel.onLocationSelected(lat, lng)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Current Location Button
            FloatingActionButton(
                onClick = {
                    if (PermissionUtils.hasLocationPermission(context)) {
                        getCurrentLocation(context) { lat, lng ->
                            viewModel.onLocationSelected(lat, lng)
                        }
                    } else {
                        locationPermissionLauncher.launch(PermissionUtils.locationPermissions)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = if (uiState.selectedLatitude != null) 100.dp else 0.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }

            // Selected Location Info
            if (uiState.selectedLatitude != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (uiState.isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        Text(
                            text = uiState.selectedAddress.ifBlank { "Loading address..." },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showBottomSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save This Location")
                        }
                    }
                }
            }

            // Help Text
            if (uiState.selectedLatitude == null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Tap on the map to select a destination",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Save Destination Bottom Sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Save Destination",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.destinationName,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Destination Name (optional)") },
                    placeholder = { Text("Home, Work, Gym...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.selectedAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                RadiusSlider(
                    value = uiState.radiusMeters,
                    onValueChange = viewModel::onRadiusChanged
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.saveDestination()
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Destination")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Error Snackbar
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@Composable
fun OSMMapView(
    context: Context,
    selectedLatitude: Double?,
    selectedLongitude: Double?,
    radiusMeters: Float,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {

                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(30.033333, 31.233334)) // Default center (you can adjust)
                // Add tap listener
                overlays.add(object : Overlay() {
                    override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                        if (e != null && mapView != null) {
                            val projection = mapView.projection
                            val geoPoint =
                                projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                            onLocationSelected(geoPoint.latitude, geoPoint.longitude)
                            return true
                        }
                        return false
                    }
                })

                mapView = this
            }
        },
        update = { map ->
            // Update marker when selection changes
            if (selectedLatitude != null && selectedLongitude != null) {
                // Remove old marker
                marker?.let { map.overlays.remove(it) }

                // Add new marker
                val newMarker = Marker(map).apply {
                    position = GeoPoint(selectedLatitude, selectedLongitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Selected Location"
                    try {
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_location)
                    } catch (e: Exception) {
                        // Use default marker if custom icon fails
                    }
                }
                map.overlays.add(newMarker)
                marker = newMarker

                // Center map on selection
                map.controller.animateTo(GeoPoint(selectedLatitude, selectedLongitude))
                map.controller.setCenter(GeoPoint(selectedLatitude, selectedLongitude))
                map.invalidate()
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
}

private fun getCurrentLocation(context: Context, onLocationReceived: (Double, Double) -> Unit) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(it.latitude, it.longitude)
            }
        }
    } catch (e: SecurityException) {
        // Permission not granted
    }
}
