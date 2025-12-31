package com.example.traveldistancemeasurer.data.mapper

import com.example.traveldistancemeasurer.database.GpsPoint as DbGpsPoint
import com.example.traveldistancemeasurer.database.Trip as DbTrip
import com.example.traveldistancemeasurer.domain.model.GpsPoint as DomainGpsPoint
import com.example.traveldistancemeasurer.domain.model.Trip as DomainTrip

/**
 * Mapper object to convert between database models and domain models
 */
object TripMapper {
    /**
     * Convert database Trip to domain Trip
     */
    fun DbTrip.toDomain(gpsPoints: List<DomainGpsPoint> = emptyList()): DomainTrip {
        return DomainTrip(
            id = this.id,
            startTime = this.startTime,
            endTime = this.endTime,
            totalDistance = this.totalDistance,
            duration = this.duration,
            isActive = this.isActive == 1L, // Convert Long to Boolean
            name = this.name,
            gpsPoints = gpsPoints
        )
    }

    /**
     * Convert database GpsPoint to domain GpsPoint
     */
    fun DbGpsPoint.toDomain(): DomainGpsPoint {
        return DomainGpsPoint(
            latitude = this.latitude,
            longitude = this.longitude,
            timestamp = this.timestamp
        )
    }

    /**
     * Convert list of database GpsPoints to domain GpsPoints
     */
    fun List<DbGpsPoint>.toDomainGpsPoints(): List<DomainGpsPoint> {
        return this.map { it.toDomain() }
    }

    /**
     * Convert list of database Trips to domain Trips
     */
    fun List<DbTrip>.toDomainTrips(): List<DomainTrip> {
        return this.map { it.toDomain() }
    }

    /**
     * Convert domain GpsPoint to database-friendly parameters
     */
    fun DomainGpsPoint.toDbParams(): Triple<Double, Double, Long> {
        return Triple(latitude, longitude, timestamp)
    }
}
