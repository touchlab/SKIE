package co.touchlab.skie.gradle.util

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal fun KotlinSourceSet.generateKotlinCode(fileName: String, code: String, project: Project) {
    val generatedDirectory = project.layout.buildDirectory.dir("generated/sources/skie/$name").get().asFile

    generatedDirectory.mkdirs()

    val file = generatedDirectory.resolve(fileName)

    file.writeText(code)

    kotlin {
        srcDir(generatedDirectory)
    }
}
