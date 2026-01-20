package com.example.greenalert.ui.manualinput

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.greenalert.ui.components.RadiusSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    onNavigateBack: () -> Unit,
    initialAddress: String? = null,
    initialLat: Double? = null,
    initialLng: Double? = null,
    viewModel: ManualInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle initial values (from Maps share)
    LaunchedEffect(initialAddress, initialLat, initialLng) {
        if (initialAddress != null && initialAddress.isNotBlank()) {
            viewModel.setInitialAddress(initialAddress)
        } else if (initialLat != null && initialLng != null) {
            viewModel.setInitialCoordinates(initialLat, initialLng)
        }
    }
    
    // Navigate back after save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }
    
    // Show error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Address") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name Input
            OutlinedTextField(
                value = uiState.destinationName,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Destination Name (optional)") },
                placeholder = { Text("Home, Work, Gym...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Address Input
            OutlinedTextField(
                value = uiState.addressInput,
                onValueChange = viewModel::onAddressChanged,
                label = { Text("Address") },
                placeholder = { Text("Enter street address, city, or place name") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = viewModel::geocodeAddress,
                        enabled = !uiState.isGeocoding
                    ) {
                        if (uiState.isGeocoding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                },
                singleLine = true
            )
            
            // Search Button
            Button(
                onClick = viewModel::geocodeAddress,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.addressInput.isNotBlank() && !uiState.isGeocoding
            ) {
                if (uiState.isGeocoding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Search Address")
            }
            
            // Geocoded Result
            uiState.geocodedResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📍 Location Found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Lat: ${"%.4f".format(result.latitude)}, Lng: ${"%.4f".format(result.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Radius Slider
                RadiusSlider(
                    value = uiState.radiusMeters,
                    onValueChange = viewModel::onRadiusChanged
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save Button
                Button(
                    onClick = viewModel::saveDestination,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Destination")
                    }
                }
            }
            
            // Help Text when no result
            if (uiState.geocodedResult == null && !uiState.isGeocoding) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Enter an address and tap Search to find the location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
