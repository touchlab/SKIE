import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.konan.target.KonanTarget
import co.touchlab.skie.gradle.util.kotlinNativeCompilerHome

plugins {
    id("skie-jvm")
    id("skie-buildconfig")
}

buildConfig {
    buildConfigField(
        type = "String",
        name = "BUILD",
        value = "\"${layout.buildDirectory.get().asFile.absolutePath}\"",
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

    attributes.attribute(
        KotlinPlatformType.attribute,
        KotlinPlatformType.native
    )
    attributes.attribute(KotlinNativeTarget.konanTargetAttribute, konanTarget)
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
}

val acceptanceTestExportedDependencies: Configuration = configurations.create("acceptanceTestExportedDependencies") {
    isCanBeConsumed = false
    isCanBeResolved = true

    exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

    val konanTarget = when (val architecture = "uname -m".let(ProcessGroovyMethods::execute).let(ProcessGroovyMethods::getText).trim()) {
        "arm64" -> KonanTarget.MACOS_ARM64.name
        "x86_64" -> KonanTarget.MACOS_X64.name
        else -> error("Unsupported architecture: $architecture")
    }

    attributes.attribute(
        KotlinPlatformType.attribute,
        KotlinPlatformType.native
    )
    attributes.attribute(KotlinNativeTarget.konanTargetAttribute, konanTarget)
    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
}

dependencies {
    testImplementation(projects.acceptanceTests.framework)
    testImplementation("co.touchlab.skie:configuration-annotations")
    testImplementation(libs.kotlinPoet)
    testImplementation(libs.kotlin.native.compiler.embeddable)

    testImplementation("co.touchlab.skie:configuration-api")
    testImplementation("co.touchlab.skie:generator")
    testImplementation("co.touchlab.skie:kotlin-plugin")
    testImplementation("co.touchlab.skie:api")
    testImplementation("co.touchlab.skie:spi")

    acceptanceTestDependencies("co.touchlab.skie:configuration-annotations")
    acceptanceTestDependencies("co.touchlab.skie:kotlin")
    acceptanceTestDependencies(projects.acceptanceTests.typeMapping.nonexportedDependency)

    acceptanceTestExportedDependencies(projects.acceptanceTests.typeMapping.exportedDependency)
}

tasks.test {
    dependsOn(acceptanceTestDependencies.buildDependencies)
    dependsOn(acceptanceTestExportedDependencies.buildDependencies)

    maxHeapSize = "16g"
}

buildConfig {
    fun Collection<File>.toListString(): String =
        this.joinToString(", ") { "\"${it.absolutePath}\"" }

    val resolvedDependencies = acceptanceTestDependencies.resolve() + acceptanceTestExportedDependencies.resolve()
    val exportedDependencies = acceptanceTestDependencies.filter { it.path.contains("plugin/runtime/kotlin") }.toList() +
        acceptanceTestExportedDependencies.resolve()

    buildConfigField(
        type = "co.touchlab.skie.acceptancetests.util.StringArray",
        name = "DEPENDENCIES",
        value = "arrayOf(${resolvedDependencies.toListString()})",
    )

    buildConfigField(
        type = "co.touchlab.skie.acceptancetests.util.StringArray",
        name = "EXPORTED_DEPENDENCIES",
        value = "arrayOf(${exportedDependencies.toListString()})",
    )

    buildConfigField(
        type = "String",
        name = "KONAN_HOME",
        value = "\"${kotlinNativeCompilerHome.path}\"",
    )
    buildConfigField(
        type = "String",
        name = "TEST_RESOURCES",
        value = "\"${layout.projectDirectory.dir("src/test/resources").asFile.absolutePath}\"",
    )
}
