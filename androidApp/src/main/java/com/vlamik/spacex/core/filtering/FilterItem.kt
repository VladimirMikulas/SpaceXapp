package com.vlamik.spacex.core.filtering

data class FilterItem(
    val key: String,                // FilterConstants.KEY_HEIGHT
    val displayName: String,        // "Height"
    val values: List<String>,       // ["Under 50m", "50-100m", "Over 100m"]
    val extraParams: Map<String, String> = emptyMap() // ["unit" to "m"]
)