plugins {
    `kotlin-dsl`
}

group = "com.vlamik.news.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.gradle.plugin.android)
    compileOnly(libs.gradle.plugin.kotlin)
}

gradlePlugin {
    plugins {
        register("coroutines") {
            id = "template.coroutines"
            implementationClass = "CoroutinesConventionPlugin"
        }

        register("kotlinFeature") {
            id = "template.kotlin.feature"
            implementationClass = "KotlinFeatureConventionPlugin"
        }
    }
}
