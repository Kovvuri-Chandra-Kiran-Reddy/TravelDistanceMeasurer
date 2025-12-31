package com.example.traveldistancemeasurer.ui.screens.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traveldistancemeasurer.domain.model.TrackingState
import com.example.traveldistancemeasurer.domain.repository.TripRepository
import com.example.traveldistancemeasurer.service.LocationTrackingService
import com.example.traveldistancemeasurer.util.Constants
import com.example.traveldistancemeasurer.util.PermissionHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Map screen
 */
data class MapUiState(
    val hasLocationPermission: Boolean = false,
    val hasBackgroundPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val isTracking: Boolean = false,
    val currentDistance: Double = 0.0,
    val currentDuration: Long = 0,
    val pointsCount: Int = 0,
    val currentLocation: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val completedTripId: Long? = null
)

/**
 * ViewModel for the Map screen
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: TripRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
        observeTrackingState()
        startLocationUpdates()
    }

    /**
     * Check all required permissions
     */
    private fun checkPermissions() {
        _uiState.value = _uiState.value.copy(
            hasLocationPermission = PermissionHelper.hasLocationPermission(context),
            hasBackgroundPermission = PermissionHelper.hasBackgroundLocationPermission(context),
            hasNotificationPermission = PermissionHelper.hasNotificationPermission(context)
        )
    }

    /**
     * Observe tracking state from the service
     */
    private fun observeTrackingState() {
        viewModelScope.launch {
            LocationTrackingService.trackingState.collect { state ->
                when (state) {
                    is TrackingState.Idle -> {
                        tripStartTime = 0 // Reset trip start time

                        // Cancel route and timer updates when trip ends
                        routeUpdateJob?.cancel()
                        timerJob?.cancel()

                        // If we were tracking before, show the completed trip
                        val showCompletedTrip = _uiState.value.isTracking && lastCompletedTripId != null

                        _uiState.value = _uiState.value.copy(
                            isTracking = false,
                            currentDistance = 0.0,
                            currentDuration = 0,
                            pointsCount = 0,
                            routePoints = emptyList(),
                            completedTripId = if (showCompletedTrip) lastCompletedTripId else null
                        )
                    }
                    is TrackingState.Active -> {
                        // Reset trip start time for new trip
                        if (!_uiState.value.isTracking) {
                            tripStartTime = 0
                        }

                        // Store the current trip ID for later
                        lastCompletedTripId = state.tripId

                        _uiState.value = _uiState.value.copy(
                            isTracking = true,
                            currentDistance = state.currentDistance,
                            currentDuration = state.currentDuration,
                            pointsCount = state.pointsCount,
                            completedTripId = null // Clear completed trip when starting new one
                        )
                        // Continuously load route points for the active trip
                        loadRoutePointsContinuously(state.tripId)
                    }
                }
            }
        }
    }

    private var routeUpdateJob: kotlinx.coroutines.Job? = null
    private var timerJob: kotlinx.coroutines.Job? = null
    private var tripStartTime: Long = 0
    private var lastCompletedTripId: Long? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    /**
     * Continuously load GPS points for the current trip to display route
     */
    private fun loadRoutePointsContinuously(tripId: Long) {
        // Cancel previous job if any
        routeUpdateJob?.cancel()
        timerJob?.cancel()

        // Start new continuous update job for route points
        routeUpdateJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val trip = repository.getTripById(tripId)
                    trip?.let {
                        val points = it.gpsPoints.map { gpsPoint ->
                            LatLng(gpsPoint.latitude, gpsPoint.longitude)
                        }
                        _uiState.value = _uiState.value.copy(
                            routePoints = points,
                            currentLocation = points.lastOrNull()
                        )

                        // Store trip start time for duration calculation
                        if (tripStartTime == 0L) {
                            tripStartTime = trip.startTime
                        }
                    }
                } catch (e: Exception) {
                    // Handle error
                }
                // Update every 2 seconds
                kotlinx.coroutines.delay(2000)
            }
        }

        // Start timer job to update duration every second
        timerJob = viewModelScope.launch {
            while (isActive) {
                if (tripStartTime > 0) {
                    val currentDuration = System.currentTimeMillis() - tripStartTime
                    _uiState.value = _uiState.value.copy(
                        currentDuration = currentDuration
                    )
                }
                // Update every second
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    /**
     * Start location updates to track user's current location (even when not tracking)
     */
    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Only update current location when NOT tracking
                    // (when tracking, location comes from route points)
                    if (!_uiState.value.isTracking) {
                        _uiState.value = _uiState.value.copy(
                            currentLocation = LatLng(location.latitude, location.longitude)
                        )
                    }
                }
            }
        }

        // Request location updates if we have permission
        if (PermissionHelper.hasLocationPermission(context)) {
            requestLocationUpdates()
        }
    }

    /**
     * Request location updates from FusedLocationProviderClient
     */
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10000L // Update every 10 seconds when not tracking
        ).build()

        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback!!,
                    Looper.getMainLooper()
                )

                // Get last known location immediately
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        if (!_uiState.value.isTracking) {
                            _uiState.value = _uiState.value.copy(
                                currentLocation = LatLng(it.latitude, it.longitude)
                            )
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    override fun onCleared() {
        super.onCleared()
        routeUpdateJob?.cancel()
        timerJob?.cancel()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    /**
     * Start tracking - called when user presses Start button
     */
    fun onStartTracking() {
        if (!_uiState.value.hasLocationPermission) {
            return
        }

        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = Constants.ACTION_START_TRACKING
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Stop tracking - called when user presses Stop button
     */
    fun onStopTracking() {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = Constants.ACTION_STOP_TRACKING
        }
        context.startService(intent)
    }

    /**
     * Called when permissions are granted
     */
    fun onPermissionsGranted() {
        checkPermissions()
        // Start location updates when permission is granted
        if (PermissionHelper.hasLocationPermission(context)) {
            requestLocationUpdates()
        }
    }

    /**
     * Dismiss completed trip dialog
     */
    fun dismissCompletedTrip() {
        _uiState.value = _uiState.value.copy(completedTripId = null)
    }

    /**
     * Get trip by ID
     */
    suspend fun getTripById(tripId: Long) = repository.getTripById(tripId)

    /**
     * Update trip details (name)
     */
    suspend fun updateTripDetails(tripId: Long, name: String?) {
        repository.updateTripDetails(tripId, name)
    }
}
