package utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.kotlinOptions(options: KotlinJvmCompilerOptions.() -> Unit) {
    this.tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            options.invoke(this)
        }
    }
}