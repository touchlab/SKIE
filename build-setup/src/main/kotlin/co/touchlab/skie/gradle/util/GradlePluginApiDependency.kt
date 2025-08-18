package co.touchlab.skie.gradle.util

import co.touchlab.skie.gradle.version.minGradleVersion
import org.gradle.api.Project

fun Project.gradlePluginApi(): String =
    "dev.gradleplugins:gradle-api:${minGradleVersion().gradle}"
