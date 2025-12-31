package com.example.traveldistancemeasurer.ui.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.traveldistancemeasurer.ui.screens.map.components.PermissionHandler
import com.example.traveldistancemeasurer.ui.screens.map.components.TrackingControls
import com.example.traveldistancemeasurer.ui.screens.map.components.TripStatsCard
import com.example.traveldistancemeasurer.ui.screens.map.components.TripSummaryDialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * Main map screen showing location tracking and trip stats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
  onNavigateToHistory: () -> Unit,
  viewModel: MapViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsState()
  val coroutineScope = rememberCoroutineScope()
  var completedTrip by remember {
    mutableStateOf<com.example.traveldistancemeasurer.domain.model.Trip?>(
      null
    )
  }

  // Load completed trip when available
  LaunchedEffect(uiState.completedTripId) {
    uiState.completedTripId?.let { tripId ->
      coroutineScope.launch {
        completedTrip = viewModel.getTripById(tripId)
      }
    }
  }

  // Default camera position (will be updated when location is available)
  val defaultPosition = LatLng(0.0, 0.0)
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(
      uiState.currentLocation ?: defaultPosition,
      15f
    )
  }

  // Update camera position when current location changes
  LaunchedEffect(uiState.currentLocation) {
    uiState.currentLocation?.let { location ->
      cameraPositionState.animate(
        update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
          location,
          16f
        ),
        durationMs = 1000
      )
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Travel Distance Measurer") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
    ) {
      // Google Map
      GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
          isMyLocationEnabled = uiState.hasLocationPermission,
          isTrafficEnabled = false
        ),
        uiSettings = MapUiSettings(
          myLocationButtonEnabled = true,
          zoomControlsEnabled = false,
          compassEnabled = true
        )
      ) {
        // Draw polyline for current trip route
        if (uiState.routePoints.isNotEmpty()) {
          Polyline(
            points = uiState.routePoints,
            color = androidx.compose.ui.graphics.Color.Blue,
            width = 10f
          )
        }

        // Add marker for current location when not tracking
        if (!uiState.isTracking) {
          uiState.currentLocation?.let { location ->
            Marker(
              state = MarkerState(position = location),
              title = "Current Location",
              snippet = "Your location"
            )
          }
        }
      }

      // Permission handler overlay (covers map when location permission missing)
      if (!uiState.hasLocationPermission) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.surface
        ) {
          PermissionHandler(
            hasLocationPermission = uiState.hasLocationPermission,
            hasBackgroundPermission = uiState.hasBackgroundPermission,
            hasNotificationPermission = uiState.hasNotificationPermission,
            onPermissionsGranted = {
              viewModel.onPermissionsGranted()
            }
          )
        }
      }

      // Keep PermissionHandler mounted even when location is granted
      // so notification permission dialog can appear
      if (uiState.hasLocationPermission && !uiState.hasNotificationPermission) {
        PermissionHandler(
          hasLocationPermission = uiState.hasLocationPermission,
          hasBackgroundPermission = uiState.hasBackgroundPermission,
          hasNotificationPermission = uiState.hasNotificationPermission,
          onPermissionsGranted = {
            viewModel.onPermissionsGranted()
          }
        )
      }

      // Trip stats card (visible when tracking)
      if (uiState.isTracking) {
        TripStatsCard(
          distance = uiState.currentDistance,
          duration = uiState.currentDuration,
          pointsCount = uiState.pointsCount,
          modifier = Modifier
              .align(Alignment.TopCenter)
              .padding(top = 8.dp)
        )
      }

      // Tracking controls
      TrackingControls(
        isTracking = uiState.isTracking,
        hasLocationPermission = uiState.hasLocationPermission,
        hasNotificationPermission = uiState.hasNotificationPermission,
        onStartClick = {
          viewModel.onStartTracking()
        },
        onStopClick = {
          viewModel.onStopTracking()
        },
        onShowTripsClick = {
          onNavigateToHistory()
        },
        modifier = Modifier.align(Alignment.BottomCenter)
      )

      // Show trip summary dialog when trip is completed
      completedTrip?.let { trip ->
        TripSummaryDialog(
          trip = trip,
          onDismiss = {
            completedTrip = null
            viewModel.dismissCompletedTrip()
          },
          onViewDetails = {
            completedTrip = null
            viewModel.dismissCompletedTrip()
            onNavigateToHistory()
          },
          onEditTrip = { name ->
            coroutineScope.launch {
              viewModel.updateTripDetails(trip.id, name)
              // Reload trip data
              completedTrip = viewModel.getTripById(trip.id)
            }
          }
        )
      }
    }
  }
}
