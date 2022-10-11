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
        value = "\"${gradle.includedBuild("core").projectDir.resolve("api/src/commonMain/kotlin")}\"",
    )
}

dependencies {
    api(libs.bundles.testing.jvm)

    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    runtimeOnly(files(kotlinNativeCompilerEmbeddable()))

    implementation("co.touchlab.swiftgen:compiler-plugin")
    implementation("co.touchlab.swiftgen:configuration")
    implementation(libs.swiftpack.api)
    implementation(libs.swiftpack.spi)
    implementation(libs.swiftlink.plugin)
}

tasks.test {
    useJUnitPlatform()
}
