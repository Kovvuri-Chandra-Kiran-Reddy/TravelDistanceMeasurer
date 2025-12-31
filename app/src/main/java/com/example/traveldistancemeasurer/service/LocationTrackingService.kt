package com.example.traveldistancemeasurer.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.traveldistancemeasurer.data.local.DatabaseDriverFactory
import com.example.traveldistancemeasurer.data.local.TripDataSource
import com.example.traveldistancemeasurer.data.repository.TripRepositoryImpl
import com.example.traveldistancemeasurer.database.TravelDatabase
import com.example.traveldistancemeasurer.domain.model.GpsPoint
import com.example.traveldistancemeasurer.domain.model.TrackingState
import com.example.traveldistancemeasurer.domain.repository.TripRepository
import com.example.traveldistancemeasurer.util.Constants
import com.example.traveldistancemeasurer.util.DistanceCalculator
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service for tracking location and calculating distance traveled
 */
class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var repository: TripRepository

    private var currentTripId: Long = 0
    private var lastLocation: Location? = null
    private var tripStartTime: Long = 0
    private var totalDistance: Double = 0.0
    private var pointsCollected: Int = 0

    companion object {
        private const val TAG = "LocationTrackingService"

        private val _trackingState = MutableStateFlow<TrackingState>(TrackingState.Idle)
        val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationHelper = NotificationHelper(this)

        // Initialize repository (would normally be injected via Hilt, but service isn't annotated)
        val driverFactory = DatabaseDriverFactory(applicationContext)
        val database = TravelDatabase(driverFactory.createDriver())
        val dataSource = TripDataSource(database)
        repository = TripRepositoryImpl(dataSource)

        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_TRACKING -> startTracking()
            Constants.ACTION_STOP_TRACKING, Constants.ACTION_STOP_FROM_NOTIFICATION -> stopTracking()
        }
        return START_STICKY
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    processLocation(location)
                }
            }
        }
    }

    private fun startTracking() {
        Log.d(TAG, "Starting tracking")

        // IMPORTANT: Start foreground service IMMEDIATELY to avoid crash
        // Android requires startForeground() within 5 seconds of startForegroundService()
        val notification = notificationHelper.buildNotification(
            distance = 0.0,
            duration = 0
        )
        startForeground(Constants.NOTIFICATION_ID, notification)

        // Now do the rest of the initialization asynchronously
        serviceScope.launch {
            try {
                // Create new trip in database
                currentTripId = repository.startTrip()
                tripStartTime = System.currentTimeMillis()
                totalDistance = 0.0
                pointsCollected = 0
                lastLocation = null

                Log.d(TAG, "Trip started with ID: $currentTripId")

                // Start location updates
                startLocationUpdates()

                // Emit active state
                _trackingState.value = TrackingState.Active(
                    tripId = currentTripId,
                    currentDistance = 0.0,
                    currentDuration = 0,
                    pointsCount = 0
                )
            } catch (e: SecurityException) {
                Log.e(TAG, "Location permission not granted", e)
                stopSelf()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting tracking", e)
                stopSelf()
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(Constants.LOCATION_FASTEST_UPDATE_INTERVAL_MS)
            setMinUpdateDistanceMeters(Constants.MINIMUM_DISTANCE_THRESHOLD_METERS)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location permission", e)
        }
    }

    private fun processLocation(location: Location) {
        // Filter out low-accuracy readings
        if (location.accuracy > Constants.MINIMUM_ACCURACY_METERS) {
            Log.d(TAG, "Location accuracy too low: ${location.accuracy}m, skipping")
            return
        }

        serviceScope.launch {
            try {
                val gpsPoint = GpsPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = location.time
                )

                // Calculate distance from last point
                lastLocation?.let { last ->
                    val distance = DistanceCalculator.calculateDistance(
                        GpsPoint(last.latitude, last.longitude, last.time),
                        gpsPoint
                    )

                    // Only add distance if it's above threshold to reduce GPS drift
                    if (distance * 1000 > Constants.MINIMUM_DISTANCE_THRESHOLD_METERS) {
                        totalDistance += distance
                        Log.d(TAG, "Distance added: ${String.format("%.3f", distance)}km, Total: ${String.format("%.2f", totalDistance)}km")
                    }
                }

                // Save GPS point to database
                repository.addGpsPoint(currentTripId, gpsPoint)
                pointsCollected++

                // Update last location
                lastLocation = location

                // Calculate current duration
                val currentDuration = System.currentTimeMillis() - tripStartTime

                // Update notification
                notificationHelper.updateNotification(totalDistance, currentDuration)

                // Emit updated state
                _trackingState.value = TrackingState.Active(
                    tripId = currentTripId,
                    currentDistance = totalDistance,
                    currentDuration = currentDuration,
                    pointsCount = pointsCollected
                )

                Log.d(TAG, "Location processed: (${location.latitude}, ${location.longitude}), Points: $pointsCollected")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing location", e)
            }
        }
    }

    private fun stopTracking() {
        Log.d(TAG, "Stopping tracking")

        serviceScope.launch {
            try {
                // Stop location updates
                fusedLocationClient.removeLocationUpdates(locationCallback)

                // Calculate final duration
                val duration = System.currentTimeMillis() - tripStartTime

                // Save final trip data
                repository.endTrip(
                    tripId = currentTripId,
                    distance = totalDistance,
                    duration = duration
                )

                Log.d(TAG, "Trip ended - Distance: ${String.format("%.2f", totalDistance)}km, Duration: ${duration}ms, Points: $pointsCollected")

                // Emit idle state
                _trackingState.value = TrackingState.Idle

                // Stop foreground service
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping tracking", e)
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
}
