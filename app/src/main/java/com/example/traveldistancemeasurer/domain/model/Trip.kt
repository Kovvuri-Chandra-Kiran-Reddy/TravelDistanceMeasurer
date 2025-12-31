package com.example.traveldistancemeasurer.domain.model

/**
 * Domain model representing a trip
 *
 * @property id Unique trip identifier
 * @property startTime Unix timestamp (milliseconds) when trip started
 * @property endTime Unix timestamp (milliseconds) when trip ended, null if trip is active
 * @property totalDistance Total distance traveled in kilometers
 * @property duration Total duration of the trip in milliseconds
 * @property isActive Whether this trip is currently being tracked
 * @property name Optional name/title for the trip
 * @property gpsPoints List of GPS points collected during the trip
 */
data class Trip(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val totalDistance: Double = 0.0,
    val duration: Long = 0,
    val isActive: Boolean = false,
    val name: String? = null,
    val gpsPoints: List<GpsPoint> = emptyList()
)
