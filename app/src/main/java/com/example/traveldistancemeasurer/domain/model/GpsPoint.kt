package com.example.traveldistancemeasurer.domain.model

/**
 * Domain model representing a GPS coordinate point
 *
 * @property latitude Latitude coordinate
 * @property longitude Longitude coordinate
 * @property timestamp Unix timestamp (milliseconds) when this point was recorded
 */
data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
