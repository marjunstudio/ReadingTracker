package com.example.readingtracker.data.database

import androidx.room.TypeConverter
import com.example.readingtracker.data.model.BookStatus
import com.example.readingtracker.data.model.PurposeCategory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    
    @TypeConverter
    fun fromBookStatus(status: BookStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toBookStatus(status: String): BookStatus {
        return BookStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromPurposeCategory(category: PurposeCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toPurposeCategory(category: String): PurposeCategory {
        return PurposeCategory.valueOf(category)
    }
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }
    
    @TypeConverter
    fun toLocalDateTime(millis: Long?): LocalDateTime? {
        return millis?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }
}