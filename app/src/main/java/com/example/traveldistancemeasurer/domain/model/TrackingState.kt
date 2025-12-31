package com.example.traveldistancemeasurer.domain.model

/**
 * Represents the current state of location tracking
 */
sealed class TrackingState {
    /**
     * No active tracking
     */
    data object Idle : TrackingState()

    /**
     * Actively tracking a trip
     *
     * @property tripId ID of the trip being tracked
     * @property currentDistance Current distance traveled in kilometers
     * @property currentDuration Current duration in milliseconds
     * @property pointsCount Number of GPS points collected so far
     */
    data class Active(
        val tripId: Long,
        val currentDistance: Double,
        val currentDuration: Long,
        val pointsCount: Int
    ) : TrackingState()
}
