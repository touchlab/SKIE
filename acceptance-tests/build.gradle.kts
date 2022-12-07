import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages

plugins {
    id("skie-jvm")
    id("skie-buildconfig")
}

val acceptanceTestsDirectory: File = layout.projectDirectory.dir("src/test/resources").asFile

buildConfig {
    buildConfigField(
        type = "String",
        name = "RESOURCES",
        value = "\"${acceptanceTestsDirectory.absolutePath}\"",
    )

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

dependencies {
    testImplementation(projects.acceptanceTests.framework)
    testImplementation("co.touchlab.skie:configuration-annotations")

    acceptanceTestDependencies("co.touchlab.skie:configuration-annotations")
    acceptanceTestDependencies("co.touchlab.skie:kotlin")
}

tasks.test {
    dependsOn(acceptanceTestDependencies.buildDependencies)

    maxHeapSize = "16g"
}

buildConfig {
    fun Collection<File>.toListString(): String =
        this.joinToString(", ") { "\"${it.absolutePath}\"" }

    val resolvedDependencies = acceptanceTestDependencies.resolve()
    val exportedDependencies = acceptanceTestDependencies.filter { it.path.contains("plugin/runtime/kotlin") }.toList()

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
}

tasks.register<ReformatPackagesInAcceptanceTests>("reformatPackagesInAcceptanceTests") {
    directory = acceptanceTestsDirectory
}

abstract class ReformatPackagesInAcceptanceTests : DefaultTask() {

    @InputDirectory
    lateinit var directory: File

    @TaskAction
    fun execute() {
        directory.resolve("tests")
            .walkTopDown()
            .filter { it.extension == "kt" }
            .forEach {
                reformatPackage(it)
            }
    }

    private fun reformatPackage(file: File) {
        val lines = file.readLines()

        val indexOfLineWithPackageDeclaration = lines.indexOfFirst { it.trimStart().startsWith("package ") }

        val modifiedLines = lines.mapIndexed { index, line ->
            if (index == indexOfLineWithPackageDeclaration) {
                file.correctPackageDeclaration()
            } else {
                line
            }
        }

        val modifiedText = modifiedLines.joinToString(System.lineSeparator(), postfix = System.lineSeparator())

        file.writeText(modifiedText)
    }

    private fun File.correctPackageDeclaration(): String {
        val pathComponents = this.getRelativePathComponents(directory)

        val packageName = pathComponents.joinToString(".") { "`$it`" }

        return "package $packageName"
    }

    private fun File.getRelativePathComponents(base: File): List<String> {
        require(this.toPath().startsWith(base.toPath()) && this != base) {
            "File $this must be located inside ${base}."
        }

        val pathComponents = mutableListOf<String>()

        var currentDirectory = this.parentFile
        while (currentDirectory != base) {
            pathComponents.add(currentDirectory.name)

            currentDirectory = currentDirectory.parentFile
        }

        return pathComponents.reversed()
    }
}
