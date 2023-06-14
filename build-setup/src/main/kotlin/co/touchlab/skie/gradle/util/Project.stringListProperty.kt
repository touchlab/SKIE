package co.touchlab.skie.gradle.util

import org.gradle.api.Project

fun Project.stringListProperty(name: String, delimiter: Char = ','): List<String> {
    val rawValue = project.property(name) as String
    return rawValue.split(delimiter).map { it.trim() }.filter { it.isNotEmpty() }
}
