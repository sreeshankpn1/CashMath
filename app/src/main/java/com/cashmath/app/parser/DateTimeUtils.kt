package com.cashmath.app.parser

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale

enum class DateFilterType {
    TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, CUSTOM_RANGE
}

object DateTimeUtils {
    private val zoneId = ZoneId.systemDefault()
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    private val sectionDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

    fun formatTransactionTime(timestamp: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId)
        val today = LocalDate.now(zoneId)
        val date = dt.toLocalDate()

        return when {
            date == today -> "Today, ${dt.format(timeFormatter)}"
            date == today.minusDays(1) -> "Yesterday, ${dt.format(timeFormatter)}"
            else -> "${dt.format(dateFormatter)}, ${dt.format(timeFormatter)}"
        }
    }

    fun formatDateHeader(timestamp: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId)
        val today = LocalDate.now(zoneId)
        val date = dt.toLocalDate()

        return when {
            date == today -> "TODAY"
            date == today.minusDays(1) -> "YESTERDAY"
            else -> date.format(sectionDateFormatter).uppercase(Locale.ENGLISH)
        }
    }

    fun getStartOfDay(timestamp: Long): Long {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId)
            .toLocalDate()
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun getDateBounds(filterType: DateFilterType, customRange: Pair<Long, Long>? = null): Pair<Long, Long> {
        val now = LocalDateTime.now(zoneId)
        return when (filterType) {
            DateFilterType.TODAY -> {
                val start = now.toLocalDate().atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.toLocalDate().atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                Pair(start, end)
            }
            DateFilterType.YESTERDAY -> {
                val yesterday = now.toLocalDate().minusDays(1)
                val start = yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = yesterday.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                Pair(start, end)
            }
            DateFilterType.THIS_WEEK -> {
                val start = now.toLocalDate().minusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.toLocalDate().atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                Pair(start, end)
            }
            DateFilterType.THIS_MONTH -> {
                val start = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = now.toLocalDate().atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                Pair(start, end)
            }
            DateFilterType.LAST_MONTH -> {
                val lastMonth = now.minusMonths(1)
                val start = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneId).toInstant().toEpochMilli()
                val end = lastMonth.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
                Pair(start, end)
            }
            DateFilterType.CUSTOM_RANGE -> customRange ?: Pair(0L, Long.MAX_VALUE)
        }
    }

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }
}
