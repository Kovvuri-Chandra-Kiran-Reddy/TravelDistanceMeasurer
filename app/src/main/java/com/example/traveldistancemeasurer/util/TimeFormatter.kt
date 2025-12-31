package com.example.traveldistancemeasurer.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility object for formatting time and durations
 */
object TimeFormatter {
    /**
     * Format duration in milliseconds to HH:MM:SS format
     *
     * @param durationMs Duration in milliseconds
     * @return Formatted duration string (e.g., "01:23:45")
     */
    fun formatDuration(durationMs: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Format timestamp to date and time
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date and time string (e.g., "Dec 31, 2025 10:30 AM")
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Format timestamp to date only
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string (e.g., "Dec 31, 2025")
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Format timestamp to time only
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted time string (e.g., "10:30 AM")
     */
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
