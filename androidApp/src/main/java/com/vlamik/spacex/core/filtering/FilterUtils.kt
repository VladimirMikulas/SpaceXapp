package com.vlamik.spacex.core.filtering


import android.content.Context
import com.vlamik.core.domain.models.datePattern
import com.vlamik.spacex.R
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

object FilterUtils {
    fun generateRanges(
        values: List<Double>,
        context: Context,
        unit: String = ""
    ): List<String> {
        if (values.isEmpty()) return emptyList()

        val sorted = values.sorted()
        val min = sorted.first()
        val max = sorted.last()
        val step = (max - min) / 3

        return listOf(
            context.getString(R.string.filter_under, "%.1f${unit}").format(min + step),
            context.getString(
                R.string.filter_range,
                "%.1f${unit}".format(min + step),
                "%.1f${unit}".format(max - step)
            ),
            context.getString(R.string.filter_over, "%.1f${unit}".format(max - step))
        )
    }

    fun generateYearRanges(
        dates: List<String>,
        context: Context
    ): List<String> {
        val years = dates.mapNotNull { extractYear(it) }
        if (years.isEmpty()) return emptyList()

        val min = years.min()
        val max = years.max()
        val step = ((max - min) / 3).coerceAtLeast(1)

        return listOf(
            context.getString(R.string.filter_before, (min + step).toString()),
            context.getString(
                R.string.filter_range,
                (min + step).toString(),
                (max - step).toString()
            ),
            context.getString(R.string.filter_after, (max - step).toString())
        )
    }

    fun extractYear(date: String): Int? {
        return try {
            DateTimeFormatter.ofPattern(datePattern)
                .parse(date)?.get(ChronoField.YEAR)
        } catch (e: Exception) {
            null
        }
    }
}