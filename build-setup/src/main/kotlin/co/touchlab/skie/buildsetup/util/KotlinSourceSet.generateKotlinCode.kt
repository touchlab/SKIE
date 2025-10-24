package co.touchlab.skie.buildsetup.util

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

fun KotlinSourceSet.generateKotlinCode(fileName: String, code: String) {
    val generatedDirectory = project.layout.buildDirectory.dir("generated/sources/skie/$name").get().asFile

    generatedDirectory.mkdirs()

    val file = generatedDirectory.resolve(fileName)

    file.writeText(code)

    kotlin {
        srcDir(generatedDirectory)
    }
}
