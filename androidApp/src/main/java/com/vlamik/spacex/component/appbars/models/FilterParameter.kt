package com.vlamik.spacex.component.appbars.models

data class FilterParameter(
    val key: String,       // Unique key (example "name", "first_flight")
    val displayName: String, // Display name (example "Name", "First Flight")
    val values: List<String>
)