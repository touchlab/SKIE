import co.touchlab.skie.gradle.extractedKotlinNativeCompilerEmbeddable
import co.touchlab.skie.gradle.kotlinNativeCompilerEmbeddable
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.buildconfig)
}

buildConfig {
    packageName(("${project.group}.${project.name}").replace("-", "_"))
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
    buildConfigField(
        type = "String",
        name = "SWIFT_GEN_API",
        value = "\"${gradle.includedBuild("plugin").projectDir.resolve("generator/configuration-annotations/src/commonMain/kotlin")}\"",
    )
}

dependencies {
    api(libs.bundles.testing.jvm)

    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    runtimeOnly(files(kotlinNativeCompilerEmbeddable()))

    implementation("co.touchlab.skie:generator")
    implementation("co.touchlab.skie:kotlin-plugin")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
    }
}

tasks.test {
    useJUnitPlatform()
}
