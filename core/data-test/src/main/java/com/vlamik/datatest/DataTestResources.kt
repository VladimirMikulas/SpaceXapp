package com.vlamik.datatest

object DataTestResources {
    fun rocketListJson(): String =
        loadJsonResource("rocketList")

    private fun loadJsonResource(fileName: String) =
        javaClass.classLoader!!
            .getResource("$fileName.json")!!
            .readText()
}
