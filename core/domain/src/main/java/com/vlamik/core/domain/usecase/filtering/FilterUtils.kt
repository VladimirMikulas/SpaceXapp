package com.vlamik.core.domain.usecase.filtering


import com.vlamik.core.domain.models.datePattern
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

// These classes store the numerical boundaries of the ranges, not their string representations.
data class DoubleRangeInfo(val min: Double, val max: Double, val step: Double)
data class IntRangeInfo(val min: Int, val max: Int, val step: Int)


object FilterUtils {

    /**
     * Generates information about numerical ranges based on a list of values.
     * Returns DoubleRangeInfo containing min, max, and step for dividing into 3 ranges.
     * No longer uses Context and does not return strings.
     */
    fun generateDoubleRangeInfo(values: List<Double>): DoubleRangeInfo? {
        if (values.isEmpty()) return null

        val sorted = values.sorted()
        val min = sorted.first()
        val max = sorted.last()
        // Prevent division by zero if all values are the same
        val step = if (max == min) 1.0 else (max - min) / 3.0

        return DoubleRangeInfo(min, max, step)
    }

    /**
     * Generates information about year ranges based on a list of date strings.
     * Returns IntRangeInfo containing min, max year, and step for dividing into 3 ranges.
     * No longer uses Context and does not return strings.
     */
    fun generateYearRangeInfo(dates: List<String>): IntRangeInfo? {
        val years = dates.mapNotNull { extractYear(it) }
        if (years.isEmpty()) return null

        val min = years.minOrNull() ?: return null // Use minOrNull/maxOrNull for empty collections
        val max = years.maxOrNull() ?: return null

        // Prevent division by zero if all years are the same
        val step = if (max == min) 1 else ((max - min) / 3).coerceAtLeast(1)

        return IntRangeInfo(min, max, step)
    }

    /**
     * Extracts the year from a date string.
     * Remains the same as it does not depend on Context.
     */
    fun extractYear(date: String): Int? {
        return try {
            DateTimeFormatter.ofPattern(datePattern)
                .parse(date).get(ChronoField.YEAR)
        } catch (e: Exception) {
            null
        }
    }
}