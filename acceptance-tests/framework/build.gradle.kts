import co.touchlab.skie.gradle.util.extractedKotlinNativeCompilerEmbeddable
import co.touchlab.skie.gradle.util.kotlinNativeCompilerEmbeddable
import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("skie-jvm")
    id("skie-buildconfig")
}

buildConfig {
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/main/resources").asFile.absolutePath}\"",
    )
}

skieJvm {
    areContextReceiversEnabled.set(true)
}

val acceptanceTestDependencies: Configuration = configurations.create("acceptanceTestDependencies") {
    isCanBeConsumed = false
    isCanBeResolved = true

    exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

    val konanTarget = when (val architecture = "uname -m".let(ProcessGroovyMethods::execute).let(ProcessGroovyMethods::getText).trim()) {
        "arm64" -> KonanTarget.MACOS_ARM64.name
        "x86_64" -> KonanTarget.MACOS_X64.name
        else -> error("Unsupported architecture: $architecture")
    }

    attributes.attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
    attributes.attribute(KotlinNativeTarget.konanTargetAttribute, konanTarget)
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
}

dependencies {
    api(libs.bundles.testing.jvm)

    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    runtimeOnly(kotlinNativeCompilerEmbeddable())

    implementation("co.touchlab.skie:configuration-api")
    implementation("co.touchlab.skie:generator")
    implementation("co.touchlab.skie:kotlin-plugin")

    acceptanceTestDependencies("co.touchlab.skie:configuration-annotations")
    acceptanceTestDependencies("co.touchlab.skie:kotlin")
}

buildConfig {
    fun Collection<File>.toListString(): String =
        this.joinToString(", ") { "\"${it.absolutePath}\"" }

    val resolvedDependencies = acceptanceTestDependencies.resolve()
    val exportedDependencies = acceptanceTestDependencies.filter { it.path.contains("plugin/runtime/kotlin") }.toList()

    buildConfigField(
        type = "co.touchlab.skie.acceptancetests.framework.util.StringArray",
        name = "DEPENDENCIES",
        value = "arrayOf(${resolvedDependencies.toListString()})",
    )

    buildConfigField(
        type = "co.touchlab.skie.acceptancetests.framework.util.StringArray",
        name = "EXPORTED_DEPENDENCIES",
        value = "arrayOf(${exportedDependencies.toListString()})",
    )
}
