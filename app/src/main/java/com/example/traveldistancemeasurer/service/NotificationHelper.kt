package com.example.traveldistancemeasurer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.traveldistancemeasurer.MainActivity
import com.example.traveldistancemeasurer.R
import com.example.traveldistancemeasurer.util.Constants
import com.example.traveldistancemeasurer.util.TimeFormatter

/**
 * Helper class for creating and managing foreground service notifications
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows your current trip progress including distance and duration"
                setShowBadge(true)
                enableVibration(false)
                enableLights(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Build the ongoing notification for the foreground service
     *
     * @param distance Current distance in kilometers
     * @param duration Current duration in milliseconds
     * @return Notification object
     */
    fun buildNotification(
        distance: Double,
        duration: Long
    ): Notification {
        // Intent to open the app when notification is tapped
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent to stop tracking from notification
        val stopIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = Constants.ACTION_STOP_FROM_NOTIFICATION
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Format distance and duration
        val distanceText = String.format("%.2f km", distance)
        val durationText = TimeFormatter.formatDuration(duration)

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("🚗 Trip in Progress")
            .setContentText("Distance: $distanceText • Duration: $durationText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("📍 Distance: $distanceText\n⏱️ Duration: $durationText\n\nTap to open app or stop tracking below."))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Tracking",
                stopPendingIntent
            )
            .build()
    }

    /**
     * Update the notification with new distance and duration
     */
    fun updateNotification(distance: Double, duration: Long) {
        val notification = buildNotification(distance, duration)
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }
}
