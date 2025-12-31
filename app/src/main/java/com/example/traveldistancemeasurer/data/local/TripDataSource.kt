package com.example.traveldistancemeasurer.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.traveldistancemeasurer.database.GpsPoint
import com.example.traveldistancemeasurer.database.TravelDatabase
import com.example.traveldistancemeasurer.database.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Data source class that wraps database queries
 */
class TripDataSource(private val database: TravelDatabase) {
    private val tripQueries = database.tripQueries
    private val gpsPointQueries = database.gpsPointQueries

    // Trip operations
    fun getAllTrips(): Flow<List<Trip>> {
        return tripQueries.getAllTrips()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun getActiveTrip(): Trip? {
        return tripQueries.getActiveTrip()
            .executeAsOneOrNull()
    }

    suspend fun getTripById(id: Long): Trip? {
        return tripQueries.getTripById(id)
            .executeAsOneOrNull()
    }

    suspend fun insertTrip(
        startTime: Long,
        endTime: Long?,
        totalDistance: Double,
        duration: Long,
        isActive: Boolean,
        name: String? = null
    ): Long {
        tripQueries.insertTrip(startTime, endTime, totalDistance, duration, if (isActive) 1L else 0L, name)
        return tripQueries.lastInsertRowId().executeAsOne()
    }

    suspend fun updateTrip(
        id: Long,
        endTime: Long?,
        totalDistance: Double,
        duration: Long,
        isActive: Boolean,
        name: String? = null
    ) {
        tripQueries.updateTrip(endTime, totalDistance, duration, if (isActive) 1L else 0L, name, id)
    }

    suspend fun deleteTrip(id: Long) {
        // First delete all GPS points for this trip (will be handled by CASCADE)
        gpsPointQueries.deletePointsForTrip(id)
        // Then delete the trip
        tripQueries.deleteTrip(id)
    }

    // GPS Point operations
    suspend fun getPointsForTrip(tripId: Long): List<GpsPoint> {
        return gpsPointQueries.getPointsForTrip(tripId)
            .executeAsList()
    }

    suspend fun insertGpsPoint(
        tripId: Long,
        latitude: Double,
        longitude: Double,
        timestamp: Long
    ) {
        gpsPointQueries.insertPoint(tripId, latitude, longitude, timestamp)
    }

    suspend fun getLastPointForTrip(tripId: Long): GpsPoint? {
        return gpsPointQueries.getLastPointForTrip(tripId)
            .executeAsOneOrNull()
    }

    suspend fun getPointCount(tripId: Long): Long {
        return gpsPointQueries.getPointCount(tripId)
            .executeAsOne()
    }

    suspend fun deletePointsForTrip(tripId: Long) {
        gpsPointQueries.deletePointsForTrip(tripId)
    }
}
