package com.example.traveldistancemeasurer.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.traveldistancemeasurer.database.TravelDatabase

/**
 * Factory class for creating SQLDelight database driver
 */
class DatabaseDriverFactory(private val context: Context) {
    /**
     * Create and return an Android SQLite driver for the TravelDatabase
     */
    fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = TravelDatabase.Schema,
            context = context,
            name = "travel.db"
        )
    }
}
