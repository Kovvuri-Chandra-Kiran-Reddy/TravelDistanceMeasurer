package com.example.traveldistancemeasurer.util

/**
 * Application-wide constants
 */
object Constants {
    // Location tracking constants
    const val LOCATION_UPDATE_INTERVAL_MS = 5000L // 5 seconds
    const val LOCATION_FASTEST_UPDATE_INTERVAL_MS = 3000L // 3 seconds
    const val MINIMUM_DISTANCE_THRESHOLD_METERS = 5f // 5 meters

    // Foreground service constants
    const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Location Tracking"
    const val NOTIFICATION_ID = 1001

    // Service actions
    const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
    const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
    const val ACTION_STOP_FROM_NOTIFICATION = "ACTION_STOP_FROM_NOTIFICATION"

    // Accuracy filter (in meters)
    const val MINIMUM_ACCURACY_METERS = 20f
}
