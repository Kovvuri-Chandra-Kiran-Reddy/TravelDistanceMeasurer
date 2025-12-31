package com.example.traveldistancemeasurer.di

import android.content.Context
import com.example.traveldistancemeasurer.data.local.DatabaseDriverFactory
import com.example.traveldistancemeasurer.data.local.TripDataSource
import com.example.traveldistancemeasurer.data.repository.TripRepositoryImpl
import com.example.traveldistancemeasurer.database.TravelDatabase
import com.example.traveldistancemeasurer.domain.repository.TripRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabaseDriverFactory(
        @ApplicationContext context: Context
    ): DatabaseDriverFactory {
        return DatabaseDriverFactory(context)
    }

    @Provides
    @Singleton
    fun provideTravelDatabase(
        driverFactory: DatabaseDriverFactory
    ): TravelDatabase {
        return TravelDatabase(driverFactory.createDriver())
    }

    @Provides
    @Singleton
    fun provideTripDataSource(
        database: TravelDatabase
    ): TripDataSource {
        return TripDataSource(database)
    }

    @Provides
    @Singleton
    fun provideTripRepository(
        dataSource: TripDataSource
    ): TripRepository {
        return TripRepositoryImpl(dataSource)
    }
}
