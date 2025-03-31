package com.vlamik.spacex.component.appbars.models

data class FilterState(
    val selectedFilters: Map<String, Set<String>> = emptyMap()
)