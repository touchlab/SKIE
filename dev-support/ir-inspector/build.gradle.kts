import co.touchlab.swiftgen.gradle.extractedKotlinNativeCompilerEmbeddable
import co.touchlab.swiftgen.gradle.kotlinNativeCompilerEmbeddable

plugins {
    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString().replace("-", "_"))
    buildConfigField(
        type = "String",
        name = "CODE",
        value = "\"${layout.projectDirectory.dir("src/main/resources/code").asFile.absolutePath}\"",
    )
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
}

dependencies {
    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    runtimeOnly(files(kotlinNativeCompilerEmbeddable()))

    testImplementation(libs.bundles.testing.jvm)
}

tasks.test {
    useJUnitPlatform()
}
