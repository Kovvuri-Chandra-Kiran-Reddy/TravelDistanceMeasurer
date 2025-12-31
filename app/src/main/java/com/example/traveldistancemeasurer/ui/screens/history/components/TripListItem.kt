package com.example.traveldistancemeasurer.ui.screens.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.traveldistancemeasurer.domain.model.Trip
import com.example.traveldistancemeasurer.ui.components.EditTripDialog
import com.example.traveldistancemeasurer.util.TimeFormatter

/**
 * List item showing trip details with map preview and delete option
 */
@Composable
fun TripListItem(
    trip: Trip,
    onClick: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    onEditTrip: (name: String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Colorful gradient header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Trip name or default title
                    Text(
                        text = trip.name ?: "Trip on ${TimeFormatter.formatDate(trip.startTime)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date
                    Text(
                        text = TimeFormatter.formatDate(trip.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Time range
                    Text(
                        text = if (trip.endTime != null) {
                            "${TimeFormatter.formatTime(trip.startTime)} - ${TimeFormatter.formatTime(trip.endTime)}"
                        } else {
                            TimeFormatter.formatTime(trip.startTime)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Trip",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Trip",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Content section
            Column(modifier = Modifier.padding(16.dp)) {
                // Trip stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TripStat(
                        label = "Distance",
                        value = String.format("%.2f km", trip.totalDistance),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TripStat(
                        label = "Duration",
                        value = TimeFormatter.formatDuration(trip.duration),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    // GPS Points (commented out - can be uncommented if needed)
                    // TripStat(
                    //     label = "Points",
                    //     value = trip.gpsPoints.size.toString(),
                    //     color = MaterialTheme.colorScheme.tertiary,
                    //     modifier = Modifier.weight(1f)
                    // )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // View full details button
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Full Details on Map")
                }
            }

            // Map preview (commented out for performance)
            // if (trip.gpsPoints.isNotEmpty()) {
            //     Spacer(modifier = Modifier.height(12.dp))
            //     TripMapPreview(
            //         gpsPoints = trip.gpsPoints,
            //         modifier = Modifier
            //             .fillMaxWidth()
            //             .height(150.dp)
            //     )
            // }
        }
    }

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

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Trip") },
            text = { Text("Are you sure you want to delete this trip? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(trip.id)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TripStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
