package com.example.traveldistancemeasurer.ui.screens.history.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.traveldistancemeasurer.domain.model.GpsPoint
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

/**
 * Small map preview showing the trip route with start/end markers
 */
@Composable
fun TripMapPreview(
    gpsPoints: List<GpsPoint>,
    modifier: Modifier = Modifier
) {
    if (gpsPoints.isEmpty()) {
        return
    }

    // Convert GPS points to LatLng
    val routePoints = remember(gpsPoints) {
        gpsPoints.map { LatLng(it.latitude, it.longitude) }
    }

    // Calculate bounds to fit all points
    val bounds = remember(routePoints) {
        if (routePoints.isEmpty()) {
            null
        } else {
            val boundsBuilder = LatLngBounds.Builder()
            routePoints.forEach { boundsBuilder.include(it) }
            boundsBuilder.build()
        }
    }

    // Default camera position (center of route)
    val cameraPositionState = rememberCameraPositionState {
        position = if (bounds != null) {
            CameraPosition.fromLatLngZoom(bounds.center, 12f)
        } else {
            CameraPosition.fromLatLngZoom(routePoints.first(), 12f)
        }
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    isTrafficEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                // Draw polyline for route
                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = androidx.compose.ui.graphics.Color.Blue,
                        width = 8f
                    )
                }

                // Start marker (green)
                if (routePoints.isNotEmpty()) {
                    Marker(
                        state = MarkerState(position = routePoints.first()),
                        title = "Start",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }

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
    }

    // Move camera to show all points when map is ready
    LaunchedEffect(bounds, cameraPositionState) {
        bounds?.let {
            try {
                cameraPositionState.animate(
                    update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(
                        it,
                        50 // padding in pixels
                    ),
                    durationMs = 1000
                )
            } catch (e: Exception) {
                // Ignore camera animation errors
            }
        }
    }
}
