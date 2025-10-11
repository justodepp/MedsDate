package com.medsdate.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room database.
 *
 * These converters allow Room to store and retrieve Date objects
 * by converting them to/from Long timestamps.
 */
class Converters {
    /**
     * Converts a Long timestamp to a Date object.
     *
     * @param timestamp The timestamp in milliseconds, or null
     * @return The corresponding Date object, or null
     */
    @TypeConverter
    fun fromTimestamp(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    /**
     * Converts a Date object to a Long timestamp.
     *
     * @param date The Date object, or null
     * @return The corresponding timestamp in milliseconds, or null
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
