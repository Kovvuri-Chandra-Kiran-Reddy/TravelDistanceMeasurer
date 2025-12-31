package com.example.traveldistancemeasurer.ui.screens.map.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Component to handle location permission requests with rationale
 */
@Composable
fun PermissionHandler(
    hasLocationPermission: Boolean,
    hasBackgroundPermission: Boolean,
    hasNotificationPermission: Boolean,
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var showRationale by rememberSaveable { mutableStateOf(false) }
    var showBackgroundRationale by rememberSaveable { mutableStateOf(false) }
    var showNotificationRationale by rememberSaveable { mutableStateOf(false) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            onPermissionsGranted()
            // After location permission, always show notification rationale on Android 13+
            // (Don't check hasNotificationPermission here as it may be stale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showNotificationRationale = true
            }
        } else {
            showRationale = true
        }
    }

    // Background location permission launcher (Android 10+)
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionsGranted()
        }
    }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionsGranted()
        }
    }

    // Show location permission request
    if (!hasLocationPermission && !showRationale) {
        LaunchedEffect(Unit) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Show location permission rationale
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Location Permission Required") },
            text = {
                Text(
                    "This app needs location access to track your trips and display your route on the map. " +
                    "Please grant location permission to continue.",
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        // Open app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show background permission rationale (Android 10+)
    if (hasLocationPermission && !hasBackgroundPermission && showBackgroundRationale) {
        AlertDialog(
            onDismissRequest = { showBackgroundRationale = false },
            title = { Text("Background Location") },
            text = {
                Text(
                    "To track your location even when the app is in the background or screen is off, " +
                    "please allow location access 'All the time' in the next screen.",
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackgroundRationale = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackgroundRationale = false }) {
                    Text("Skip")
                }
            }
        )
    }

    // Show notification permission rationale (Android 13+)
    // Check actual permission status, not the parameter (which may be stale)
    val actualHasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    if (showNotificationRationale && !actualHasNotificationPermission) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss - notification is required */ },
            title = { Text("🔔 Notification Permission Required") },
            text = {
                Column {
                    Text(
                        "To track your trip in the background, Android requires showing a persistent notification.",
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "The notification will show:",
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Real-time distance traveled\n• Trip duration\n• Quick stop button",
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Without this permission, you cannot start tracking trips.",
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationRationale = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ) {
                    Text("Grant Permission")
                }
            }
        )
    }

    // Prompt for background permission after starting tracking
    if (hasLocationPermission && !hasBackgroundPermission && !showBackgroundRationale) {
        LaunchedEffect(Unit) {
            showBackgroundRationale = true
        }
    }

    // Show permission required UI
    if (!hasLocationPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Location Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This app needs location access to track your trips. Please grant the permission to continue.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            ) {
                Text("Grant Permission")
            }
        }
    }
}
