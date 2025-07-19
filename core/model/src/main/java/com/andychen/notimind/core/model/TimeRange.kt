package com.andychen.notimind.core.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Represents a time range for filtering notifications.
 */
data class TimeRange(
    val startTime: Long,
    val endTime: Long
) {
    companion object {
        /**
         * Creates a TimeRange for a specific day.
         */
        fun forDay(date: LocalDate): TimeRange {
            val startDateTime = date.atStartOfDay()
            val endDateTime = date.plusDays(1).atStartOfDay()
            
            return TimeRange(
                startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            )
        }
        
        /**
         * Creates a TimeRange for a specific time period within a day.
         */
        fun forPeriod(date: LocalDate, period: TimePeriod): TimeRange {
            val (startHour, endHour) = when (period) {
                TimePeriod.MORNING -> 5 to 12
                TimePeriod.AFTERNOON -> 12 to 17
                TimePeriod.EVENING -> 17 to 22
                TimePeriod.NIGHT -> 22 to 5
                TimePeriod.ALL_DAY -> return forDay(date)
            }
            
            val startDateTime = if (period == TimePeriod.NIGHT) {
                LocalDateTime.of(date, LocalTime.of(startHour, 0))
            } else {
                LocalDateTime.of(date, LocalTime.of(startHour, 0))
            }
            
            val endDateTime = if (period == TimePeriod.NIGHT) {
                LocalDateTime.of(date.plusDays(1), LocalTime.of(endHour, 0))
            } else {
                LocalDateTime.of(date, LocalTime.of(endHour, 0))
            }
            
            return TimeRange(
                startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            )
        }
        
        /**
         * Creates a TimeRange for the last N days.
         */
        fun forLastDays(days: Int): TimeRange {
            val now = LocalDateTime.now()
            val startDateTime = now.minusDays(days.toLong()).withHour(0).withMinute(0).withSecond(0)
            
            return TimeRange(
                startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }
        
        /**
         * Creates a TimeRange for all time.
         */
        fun allTime(): TimeRange {
            return TimeRange(0, Long.MAX_VALUE)
        }
    }
}