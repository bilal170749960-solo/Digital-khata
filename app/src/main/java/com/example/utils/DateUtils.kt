package com.example.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Timezone-aware date utilities for Digital Khata.
 * Strictly operates using Pakistan Standard Time (Asia/Karachi).
 * Ensures current live dates, midnight transitions, month keys, and labels
 * are calculated dynamically from the system clock without hardcoding.
 */
object DateUtils {

    const val PAKISTAN_TIMEZONE_ID = "Asia/Karachi"
    val PAKISTAN_TIMEZONE: TimeZone = TimeZone.getTimeZone(PAKISTAN_TIMEZONE_ID)

    private const val DISPLAY_DATE_PATTERN = "dd MMM yyyy"
    private const val MONTH_KEY_PATTERN = "yyyy-MM"
    private const val MONTH_LABEL_PATTERN = "MMMM yyyy"

    /**
     * Returns the current live date in Pakistan timezone formatted as "dd MMM yyyy" (e.g. "27 Sep 2026").
     */
    fun getCurrentDateFormatted(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val zonedDateTime = ZonedDateTime.now(ZoneId.of(PAKISTAN_TIMEZONE_ID))
                val formatter = DateTimeFormatter.ofPattern(DISPLAY_DATE_PATTERN, Locale.ENGLISH)
                zonedDateTime.format(formatter)
            } catch (_: Exception) {
                fallbackFormat(DISPLAY_DATE_PATTERN, System.currentTimeMillis())
            }
        } else {
            fallbackFormat(DISPLAY_DATE_PATTERN, System.currentTimeMillis())
        }
    }

    /**
     * Returns the current live month key in Pakistan timezone formatted as "yyyy-MM" (e.g. "2026-09").
     */
    fun getCurrentMonthKey(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val zonedDateTime = ZonedDateTime.now(ZoneId.of(PAKISTAN_TIMEZONE_ID))
                val formatter = DateTimeFormatter.ofPattern(MONTH_KEY_PATTERN, Locale.ENGLISH)
                zonedDateTime.format(formatter)
            } catch (_: Exception) {
                fallbackFormat(MONTH_KEY_PATTERN, System.currentTimeMillis())
            }
        } else {
            fallbackFormat(MONTH_KEY_PATTERN, System.currentTimeMillis())
        }
    }

    /**
     * Returns the current live month label in Pakistan timezone formatted as "MMMM yyyy" (e.g. "September 2026").
     */
    fun getCurrentMonthLabel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val zonedDateTime = ZonedDateTime.now(ZoneId.of(PAKISTAN_TIMEZONE_ID))
                val formatter = DateTimeFormatter.ofPattern(MONTH_LABEL_PATTERN, Locale.ENGLISH)
                zonedDateTime.format(formatter)
            } catch (_: Exception) {
                fallbackFormat(MONTH_LABEL_PATTERN, System.currentTimeMillis())
            }
        } else {
            fallbackFormat(MONTH_LABEL_PATTERN, System.currentTimeMillis())
        }
    }

    /**
     * Returns the previous month key in Pakistan timezone (e.g. "2026-08").
     */
    fun getPreviousMonthKey(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val zonedDateTime = ZonedDateTime.now(ZoneId.of(PAKISTAN_TIMEZONE_ID)).minusMonths(1)
                val formatter = DateTimeFormatter.ofPattern(MONTH_KEY_PATTERN, Locale.ENGLISH)
                zonedDateTime.format(formatter)
            } catch (_: Exception) {
                val cal = getPakistanCalendar()
                cal.add(Calendar.MONTH, -1)
                val sdf = SimpleDateFormat(MONTH_KEY_PATTERN, Locale.ENGLISH).apply {
                    timeZone = PAKISTAN_TIMEZONE
                }
                sdf.format(cal.time)
            }
        } else {
            val cal = getPakistanCalendar()
            cal.add(Calendar.MONTH, -1)
            val sdf = SimpleDateFormat(MONTH_KEY_PATTERN, Locale.ENGLISH).apply {
                timeZone = PAKISTAN_TIMEZONE
            }
            sdf.format(cal.time)
        }
    }

    /**
     * Returns the previous month label in Pakistan timezone (e.g. "August 2026").
     */
    fun getPreviousMonthLabel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val zonedDateTime = ZonedDateTime.now(ZoneId.of(PAKISTAN_TIMEZONE_ID)).minusMonths(1)
                val formatter = DateTimeFormatter.ofPattern(MONTH_LABEL_PATTERN, Locale.ENGLISH)
                zonedDateTime.format(formatter)
            } catch (_: Exception) {
                val cal = getPakistanCalendar()
                cal.add(Calendar.MONTH, -1)
                val sdf = SimpleDateFormat(MONTH_LABEL_PATTERN, Locale.ENGLISH).apply {
                    timeZone = PAKISTAN_TIMEZONE
                }
                sdf.format(cal.time)
            }
        } else {
            val cal = getPakistanCalendar()
            cal.add(Calendar.MONTH, -1)
            val sdf = SimpleDateFormat(MONTH_LABEL_PATTERN, Locale.ENGLISH).apply {
                timeZone = PAKISTAN_TIMEZONE
            }
            sdf.format(cal.time)
        }
    }

    /**
     * Formats an epoch millisecond timestamp to "dd MMM yyyy" in Pakistan timezone.
     */
    fun formatEpochMillisToDate(timestamp: Long): String {
        return fallbackFormat(DISPLAY_DATE_PATTERN, timestamp)
    }

    /**
     * Derives the month key "yyyy-MM" from an epoch millisecond timestamp in Pakistan timezone.
     */
    fun getMonthKeyFromEpochMillis(timestamp: Long): String {
        return fallbackFormat(MONTH_KEY_PATTERN, timestamp)
    }

    /**
     * Derives the month label "MMMM yyyy" from an epoch millisecond timestamp in Pakistan timezone.
     */
    fun getMonthLabelFromEpochMillis(timestamp: Long): String {
        return fallbackFormat(MONTH_LABEL_PATTERN, timestamp)
    }

    /**
     * Returns a Calendar instance initialized to the current time in Pakistan timezone.
     */
    fun getPakistanCalendar(): Calendar {
        return Calendar.getInstance(PAKISTAN_TIMEZONE)
    }

    private fun fallbackFormat(pattern: String, timestamp: Long): String {
        val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
            timeZone = PAKISTAN_TIMEZONE
        }
        return sdf.format(Date(timestamp))
    }
}
