package com.example.traveldistancemeasurer.ui.navigation

/**
 * Sealed class representing navigation destinations in the app
 */
sealed class Screen(val route: String) {
    /**
     * Map screen - main screen with location tracking
     */
    data object Map : Screen("map")

    /**
     * History screen - list of past trips
     */
    data object History : Screen("history")

    /**
     * Trip detail screen - shows full details of a specific trip
     */
    data object TripDetail : Screen("trip_detail/{tripId}") {
        fun createRoute(tripId: Long) = "trip_detail/$tripId"
    }
}
