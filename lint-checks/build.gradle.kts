plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.bundles.lint)

    testImplementation(libs.lint.tests)
    testImplementation(libs.junit)
}

tasks.jar {
    manifest {
        attributes["Lint-Registry"] = "com.vlamik.template.lintChecks.LintRegistry"
    }
}
