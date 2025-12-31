package com.example.traveldistancemeasurer.ui.screens.tripdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.traveldistancemeasurer.ui.screens.map.MapViewModel
import com.example.traveldistancemeasurer.util.TimeFormatter
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

/**
 * Detail screen showing full trip information with map
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var trip by remember { mutableStateOf<com.example.traveldistancemeasurer.domain.model.Trip?>(null) }

    // Load trip details
    LaunchedEffect(tripId) {
        coroutineScope.launch {
            trip = viewModel.getTripById(tripId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        trip?.let { tripData ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Map showing the route
                if (tripData.gpsPoints.isNotEmpty()) {
                    val routePoints = remember(tripData.gpsPoints) {
                        tripData.gpsPoints.map { LatLng(it.latitude, it.longitude) }
                    }

                    val bounds = remember(routePoints) {
                        val boundsBuilder = LatLngBounds.Builder()
                        routePoints.forEach { boundsBuilder.include(it) }
                        boundsBuilder.build()
                    }

                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(bounds.center, 12f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                isMyLocationEnabled = false,
                                isTrafficEnabled = false
                            ),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = true,
                                myLocationButtonEnabled = false,
                                compassEnabled = true
                            )
                        ) {
                            // Draw polyline for route
                            Polyline(
                                points = routePoints,
                                color = androidx.compose.ui.graphics.Color.Blue,
                                width = 10f
                            )

                            // Start marker (green)
                            Marker(
                                state = MarkerState(position = routePoints.first()),
                                title = "Start",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )

                            // End marker (red)
                            if (routePoints.size > 1) {
                                Marker(
                                    state = MarkerState(position = routePoints.last()),
                                    title = "End",
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                                )
                            }
                        }
                    }

                    // Fit bounds to show entire route
                    LaunchedEffect(bounds, cameraPositionState) {
                        try {
                            cameraPositionState.animate(
                                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(
                                    bounds,
                                    100 // padding in pixels
                                ),
                                durationMs = 1000
                            )
                        } catch (e: Exception) {
                            // Ignore camera animation errors
                        }
                    }
                }

                // Trip statistics
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Trip name
                        Text(
                            text = tripData.name ?: "Trip on ${TimeFormatter.formatDate(tripData.startTime)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatColumn(
                                label = "Distance",
                                value = String.format("%.2f km", tripData.totalDistance),
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatColumn(
                                label = "Duration",
                                value = TimeFormatter.formatDuration(tripData.duration),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            // GPS Points (commented out - can be uncommented if needed)
                            // StatColumn(
                            //     label = "Points",
                            //     value = tripData.gpsPoints.size.toString(),
                            //     color = MaterialTheme.colorScheme.tertiary
                            // )
                        }

                        Divider()

                        InfoRow(label = "Started", value = TimeFormatter.formatDateTime(tripData.startTime))
                        tripData.endTime?.let {
                            InfoRow(label = "Ended", value = TimeFormatter.formatDateTime(it))
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
