package com.example.traveldistancemeasurer.ui.screens.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.traveldistancemeasurer.domain.model.Trip
import com.example.traveldistancemeasurer.ui.components.EditTripDialog
import com.example.traveldistancemeasurer.util.TimeFormatter

/**
 * Dialog showing trip summary after completion
 */
@Composable
fun TripSummaryDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    onEditTrip: (name: String?) -> Unit = {}
) {
    var showEditDialog by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎉 Trip Completed!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Trip",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Show trip name if available
                if (!trip.name.isNullOrBlank()) {
                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Distance
                StatCard(
                    label = "Total Distance",
                    value = String.format("%.2f km", trip.totalDistance)
                )

                // Duration
                StatCard(
                    label = "Duration",
                    value = TimeFormatter.formatDuration(trip.duration)
                )

                // GPS Points (commented out - can be uncommented if needed)
                // StatCard(
                //     label = "GPS Points",
                //     value = trip.gpsPoints.size.toString()
                // )

                // Date and time
                Text(
                    text = "Completed at ${TimeFormatter.formatDateTime(trip.endTime ?: System.currentTimeMillis())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onViewDetails) {
                Text("View Details")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    // Edit trip dialog
    if (showEditDialog) {
        EditTripDialog(
            tripName = trip.name,
            onDismiss = { showEditDialog = false },
            onSave = { name ->
                onEditTrip(name)
            }
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
