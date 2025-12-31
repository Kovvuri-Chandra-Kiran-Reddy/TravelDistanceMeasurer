package com.example.traveldistancemeasurer.domain.repository

import com.example.traveldistancemeasurer.domain.model.GpsPoint
import com.example.traveldistancemeasurer.domain.model.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for trip data operations
 */
interface TripRepository {
    /**
     * Start a new trip
     * @return ID of the newly created trip
     */
    suspend fun startTrip(): Long

    /**
     * End an active trip
     * @param tripId ID of the trip to end
     * @param distance Total distance traveled in kilometers
     * @param duration Total duration in milliseconds
     */
    suspend fun endTrip(tripId: Long, distance: Double, duration: Long)

    /**
     * Add a GPS point to a trip
     * @param tripId ID of the trip
     * @param point GPS point to add
     */
    suspend fun addGpsPoint(tripId: Long, point: GpsPoint)

    /**
     * Get the currently active trip, if any
     * @return Active trip or null
     */
    suspend fun getActiveTrip(): Trip?

    /**
     * Get all completed trips as a Flow
     * @return Flow of trip list
     */
    fun getAllTrips(): Flow<List<Trip>>

    /**
     * Get a specific trip by ID with all its GPS points
     * @param id Trip ID
     * @return Trip or null if not found
     */
    suspend fun getTripById(id: Long): Trip?

    /**
     * Delete a trip and all its GPS points
     * @param id Trip ID to delete
     */
    suspend fun deleteTrip(id: Long)

    /**
     * Get the last GPS point for a trip
     * @param tripId Trip ID
     * @return Last GPS point or null
     */
    suspend fun getLastGpsPoint(tripId: Long): GpsPoint?

    /**
     * Get count of GPS points for a trip
     * @param tripId Trip ID
     * @return Number of GPS points
     */
    suspend fun getGpsPointCount(tripId: Long): Int

    /**
     * Update trip details (name)
     * @param tripId Trip ID
     * @param name Trip name
     */
    suspend fun updateTripDetails(tripId: Long, name: String?)
}
