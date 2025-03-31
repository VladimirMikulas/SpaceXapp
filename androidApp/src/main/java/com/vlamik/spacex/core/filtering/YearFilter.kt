package com.vlamik.spacex.core.filtering

import android.content.Context
import com.vlamik.spacex.R

object YearFilter {
    fun matches(
        date: String,
        selectedRanges: Set<String>,
        context: Context
    ): Boolean {
        val year = FilterUtils.extractYear(date) ?: return false
        if (selectedRanges.isEmpty()) return true

        return selectedRanges.any { range ->
            when {
                range.startsWith(context.getString(R.string.filter_before).split("%s")[0]) ->
                    year < (range.substringAfter(" ").toIntOrNull() ?: Int.MAX_VALUE)

                range.startsWith(context.getString(R.string.filter_after).split("%s")[0]) ->
                    year > (range.substringAfter(" ").toIntOrNull() ?: Int.MIN_VALUE)

                range.contains(context.getString(R.string.filter_range).split("%s")[0]) -> {
                    val parts = range.split("-")
                    if (parts.size == 2) {
                        val start = parts[0].toIntOrNull() ?: Int.MIN_VALUE
                        val end = parts[1].toIntOrNull() ?: Int.MAX_VALUE
                        year in start..end
                    } else false
                }

                else -> false
            }
        }
    }
}