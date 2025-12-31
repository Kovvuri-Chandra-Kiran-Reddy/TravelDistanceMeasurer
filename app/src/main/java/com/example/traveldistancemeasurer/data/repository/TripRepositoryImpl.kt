package com.example.traveldistancemeasurer.data.repository

import com.example.traveldistancemeasurer.data.local.TripDataSource
import com.example.traveldistancemeasurer.data.mapper.TripMapper.toDomain
import com.example.traveldistancemeasurer.data.mapper.TripMapper.toDomainGpsPoints
import com.example.traveldistancemeasurer.data.mapper.TripMapper.toDomainTrips
import com.example.traveldistancemeasurer.domain.model.GpsPoint
import com.example.traveldistancemeasurer.domain.model.Trip
import com.example.traveldistancemeasurer.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of TripRepository
 */
class TripRepositoryImpl(
    private val dataSource: TripDataSource
) : TripRepository {

    override suspend fun startTrip(): Long {
        val startTime = System.currentTimeMillis()
        return dataSource.insertTrip(
            startTime = startTime,
            endTime = null,
            totalDistance = 0.0,
            duration = 0,
            isActive = true
        )
    }

    override suspend fun endTrip(tripId: Long, distance: Double, duration: Long) {
        val endTime = System.currentTimeMillis()
        dataSource.updateTrip(
            id = tripId,
            endTime = endTime,
            totalDistance = distance,
            duration = duration,
            isActive = false
        )
    }

    override suspend fun addGpsPoint(tripId: Long, point: GpsPoint) {
        dataSource.insertGpsPoint(
            tripId = tripId,
            latitude = point.latitude,
            longitude = point.longitude,
            timestamp = point.timestamp
        )
    }

    override suspend fun getActiveTrip(): Trip? {
        val dbTrip = dataSource.getActiveTrip() ?: return null
        val gpsPoints = dataSource.getPointsForTrip(dbTrip.id).toDomainGpsPoints()
        return dbTrip.toDomain(gpsPoints)
    }

    override fun getAllTrips(): Flow<List<Trip>> {
        return dataSource.getAllTrips().map { dbTrips ->
            dbTrips.map { dbTrip ->
                // Load GPS points for each trip to show accurate count and map preview
                val gpsPoints = dataSource.getPointsForTrip(dbTrip.id).toDomainGpsPoints()
                dbTrip.toDomain(gpsPoints)
            }
        }
    }

    override suspend fun getTripById(id: Long): Trip? {
        val dbTrip = dataSource.getTripById(id) ?: return null
        val gpsPoints = dataSource.getPointsForTrip(id).toDomainGpsPoints()
        return dbTrip.toDomain(gpsPoints)
    }

    override suspend fun deleteTrip(id: Long) {
        dataSource.deleteTrip(id)
    }

    override suspend fun getLastGpsPoint(tripId: Long): GpsPoint? {
        return dataSource.getLastPointForTrip(tripId)?.toDomain()
    }

    override suspend fun getGpsPointCount(tripId: Long): Int {
        return dataSource.getPointCount(tripId).toInt()
    }

    override suspend fun updateTripDetails(tripId: Long, name: String?) {
        val trip = dataSource.getTripById(tripId) ?: return
        dataSource.updateTrip(
            id = tripId,
            endTime = trip.endTime,
            totalDistance = trip.totalDistance,
            duration = trip.duration,
            isActive = trip.isActive == 1L,
            name = name
        )
    }
}
