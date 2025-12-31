package com.example.traveldistancemeasurer.util

import com.example.traveldistancemeasurer.domain.model.GpsPoint
import kotlin.math.*

/**
 * Utility object for calculating distances between GPS coordinates using the Haversine formula
 */
object DistanceCalculator {
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculate the distance between two GPS points using the Haversine formula
     *
     * @param point1 First GPS point
     * @param point2 Second GPS point
     * @return Distance in kilometers
     */
    fun calculateDistance(point1: GpsPoint, point2: GpsPoint): Double {
        val dLat = Math.toRadians(point2.latitude - point1.latitude)
        val dLon = Math.toRadians(point2.longitude - point1.longitude)

        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculate the distance between two GPS coordinates
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return calculateDistance(
            GpsPoint(lat1, lon1, 0),
            GpsPoint(lat2, lon2, 0)
        )
    }
}
