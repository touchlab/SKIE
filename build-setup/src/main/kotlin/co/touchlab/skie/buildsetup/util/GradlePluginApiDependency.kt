package co.touchlab.skie.buildsetup.util

import co.touchlab.skie.buildsetup.util.version.minGradleVersion
import org.gradle.api.Project

fun Project.gradlePluginApi(): String =
    "dev.gradleplugins:gradle-api:${minGradleVersion().gradle}"
