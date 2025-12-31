package com.example.traveldistancemeasurer.ui.screens.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.traveldistancemeasurer.util.TimeFormatter

/**
 * Card displaying current trip statistics
 */
@Composable
fun TripStatsCard(
    distance: Double,
    duration: Long,
    pointsCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distance
            StatItem(
                label = "Distance",
                value = String.format("%.2f km", distance),
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 8.dp)
            )

            // Duration
            StatItem(
                label = "Duration",
                value = TimeFormatter.formatDuration(duration),
                modifier = Modifier.weight(1f)
            )

            // GPS Points (commented out - can be uncommented if needed)
            // VerticalDivider(
            //     modifier = Modifier
            //         .height(48.dp)
            //         .padding(horizontal = 8.dp)
            // )
            //
            // StatItem(
            //     label = "Points",
            //     value = pointsCount.toString(),
            //     modifier = Modifier.weight(1f)
            // )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier.width(1.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
    )
}
