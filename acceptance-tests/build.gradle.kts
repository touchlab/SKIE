import org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader

plugins {
    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString())
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/test/resources").asFile.absolutePath}\"",
    )
}

dependencies {
    testImplementation(project(":acceptance-tests:framework"))
}

tasks.test {
    useJUnitPlatform()
}
