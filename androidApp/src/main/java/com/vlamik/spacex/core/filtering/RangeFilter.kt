package com.vlamik.spacex.core.filtering

import android.content.Context
import com.vlamik.spacex.R
import java.text.DecimalFormat
import java.util.Locale

object RangeFilter {
    fun matches(
        value: Double,
        selectedRanges: Set<String>,
        context: Context
    ): Boolean {
        if (selectedRanges.isEmpty()) return true

        return selectedRanges.any { range ->
            when {
                isUnderRange(range, context) -> handleUnderRange(range, value, context)
                isOverRange(range, context) -> handleOverRange(range, value, context)
                isRangeFormat(range, context) -> handleRange(range, value, context)
                else -> false
            }
        }
    }

    private fun isUnderRange(range: String, context: Context): Boolean {
        val prefix = context.getString(R.string.filter_under, "").replace("%s", "").trim()
        return range.startsWith(prefix)
    }

    private fun isOverRange(range: String, context: Context): Boolean {
        val prefix = context.getString(R.string.filter_over, "").replace("%s", "").trim()
        return range.startsWith(prefix)
    }

    private fun isRangeFormat(range: String, context: Context): Boolean {
        val separator = context.getString(R.string.filter_range_separator)
        return separator in range
    }

    private fun parseNumber(str: String): Double? {
        return try {
            val cleanStr = str.replace("[^\\d.]".toRegex(), "")
            DecimalFormat.getInstance(Locale.US).parse(cleanStr)?.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    private fun handleUnderRange(range: String, value: Double, context: Context): Boolean {
        val numberStr = range.removePrefix(context.getString(R.string.filter_under))
        val number = numberStr.trim()
            .removeSuffix(context.getString(R.string.unit_meters))
            .trim()
            .toDoubleOrNull() ?: return false

        return value <= number
    }

    private fun handleOverRange(range: String, value: Double, context: Context): Boolean {
        val numberStr = range.removePrefix(context.getString(R.string.filter_over))
        val number = parseNumber(numberStr)
        return number?.let { value > it } ?: false
    }

    private fun handleRange(range: String, value: Double, context: Context): Boolean {
        val parts = range.split(context.getString(R.string.filter_range_separator))
        if (parts.size != 2) return false

        val start = parseNumber(parts[0])
        val end = parseNumber(parts[1])

        return if (start != null && end != null) {
            value >= start && value <= end
        } else {
            false
        }
    }
}