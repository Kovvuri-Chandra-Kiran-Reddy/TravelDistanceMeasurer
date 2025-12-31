package com.example.traveldistancemeasurer.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.traveldistancemeasurer.domain.model.Trip
import com.example.traveldistancemeasurer.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the History screen
 */
data class HistoryUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the History screen
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadTrips()
    }

    /**
     * Load all trips from the repository
     */
    private fun loadTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                repository.getAllTrips()
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load trips: ${exception.message}"
                            )
                        }
                    }
                    .collect { trips ->
                        // Sort trips by start time (newest first)
                        val sortedTrips = trips
                            .filter { !it.isActive } // Only show completed trips
                            .sortedByDescending { it.startTime }

                        _uiState.update {
                            it.copy(
                                trips = sortedTrips,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load trips: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Delete a trip by ID
     */
    fun deleteTrip(tripId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTrip(tripId)
                // Trips will be automatically updated via the Flow
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete trip: ${e.message}")
                }
            }
        }
    }

    /**
     * Get trip details by ID (for future use if needed)
     */
    suspend fun getTripById(tripId: Long): Trip? {
        return try {
            repository.getTripById(tripId)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(error = "Failed to load trip: ${e.message}")
            }
            null
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Update trip details (name)
     */
    fun updateTripDetails(tripId: Long, name: String?) {
        viewModelScope.launch {
            try {
                repository.updateTripDetails(tripId, name)
                // Trips will be automatically updated via the Flow
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update trip: ${e.message}")
                }
            }
        }
    }
}
