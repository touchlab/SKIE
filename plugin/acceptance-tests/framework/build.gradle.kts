import co.touchlab.swiftgen.gradle.extractedKotlinNativeCompilerEmbeddable
import co.touchlab.swiftgen.gradle.kotlinNativeCompilerEmbeddable

plugins {
    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(project.group.toString())
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
    buildConfigField(
        type = "String",
        name = "SWIFT_GEN_API",
        value = "\"${projects.generator.configurationAnnotations.dependencyProject.projectDir.resolve("src/commonMain/kotlin")}\"",
    )
}

dependencies {
    api(libs.bundles.testing.jvm)

    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    runtimeOnly(files(kotlinNativeCompilerEmbeddable()))

    implementation(projects.configurationApi)
    implementation(projects.api)
    implementation(projects.spi)
    implementation(projects.kotlinPlugin)
}

tasks.test {
    useJUnitPlatform()
}
