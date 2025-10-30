package co.touchlab.skie.buildsetup.tests.plugins.base

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityBuildConfigPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.util.MacOsCpuArchitecture
import co.touchlab.skie.buildsetup.util.enquoted
import co.touchlab.skie.buildsetup.util.getKotlinNativeCompilerEmbeddableDependency
import co.touchlab.skie.buildsetup.util.implementation
import co.touchlab.skie.buildsetup.util.testImplementation
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Usage
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import java.io.File

abstract class BaseTestsPlugin : Plugin<Project> {

    private val testInputProperties = listOf(
        "keepTemporaryFiles",
        "KOTLIN_LINK_MODE",
        "KOTLIN_BUILD_CONFIGURATION",
    )

    override fun apply(project: Project) = with(project) {
        apply<BaseKotlinPlugin>()
        apply<UtilityMinimumTargetKotlinVersionPlugin>()
        apply<UtilityBuildConfigPlugin>()
        apply<KotlinPluginWrapper>()

        val primaryCompilerVersion = SupportedKotlinVersionProvider.getPrimaryKotlinVersion(project).compilerVersion
        val testOutputDirectory = testBuildDirectory(primaryCompilerVersion)

        val (testDependencies, exportedTestDependencies) = configureDependencies(primaryCompilerVersion)
        configureBuildConfig(testDependencies, exportedTestDependencies, testOutputDirectory)
        configureTestTask(testDependencies, exportedTestDependencies, testOutputDirectory)
    }

    private fun Project.configureDependencies(compilerVersion: KotlinToolingVersion): Pair<Configuration, Configuration> {
        val exportedTestDependencies = maybeCreateTestDependencyConfiguration(project, "testExportedDependencies").apply {
            isTransitive = false
        }

        val testDependencies = maybeCreateTestDependencyConfiguration(project, "testDependencies").apply {
            extendsFrom(exportedTestDependencies)
        }

        dependencies {
            testImplementation(getKotlinNativeCompilerEmbeddableDependency(compilerVersion))
        }

        dependencies {
            testImplementation(project(":acceptance-tests:acceptance-tests-framework"))
            implementation(project(":common:util"))

            testDependencies(project(":common:configuration:configuration-annotations"))
            exportedTestDependencies(project(":runtime:runtime-kotlin"))
        }

        return testDependencies to exportedTestDependencies
    }

    private fun Project.configureBuildConfig(
        testDependencies: Configuration,
        exportedTestDependencies: Configuration,
        testOutputDirectory: Provider<Directory>,
    ) {
        extensions.configure<BuildConfigExtension> {
            className.set("TestBuildConfig")

            buildConfigField(
                type = "String",
                name = "BUILD",
                value = testOutputDirectory.map { it.asFile.absolutePath.enquoted() },
            )

            buildConfigField(
                type = "co.touchlab.skie.util.StringArray",
                name = "DEPENDENCIES",
                value = provider { testDependencies.resolve() }.map { "arrayOf(${it.toListString()})" },
            )

            buildConfigField(
                type = "co.touchlab.skie.util.StringArray",
                name = "EXPORTED_DEPENDENCIES",
                value = provider { exportedTestDependencies.resolve() }.map { "arrayOf(${it.toListString()})" },
            )
        }
    }

    private fun Project.configureTestTask(
        testDependencies: Configuration,
        exportedTestDependencies: Configuration,
        testOutputDirectory: Provider<Directory>,
    ) {
        tasks.named("test", Test::class.java).configure {
            dependsOn(
                testDependencies.buildDependencies,
                exportedTestDependencies.buildDependencies,
            )

            testInputProperties.forEach {
                inputs.property(it, System.getenv(it)).optional(true)
            }

            outputs.dir(testOutputDirectory)

            maxHeapSize = "12g"

            testLogging {
                showStandardStreams = true
            }
        }
    }

    private fun Project.testBuildDirectory(kotlinToolingVersion: KotlinToolingVersion): Provider<Directory> =
        project.layout.buildDirectory.map { it.dir(kotlinToolingVersion.toString()) }

    private fun Collection<File>.toListString(): String =
        this.joinToString(", ") { it.absolutePath.enquoted() }

    companion object {

        fun maybeCreateTestDependencyConfiguration(
            project: Project,
            name: String,
            konanTarget: Provider<String> = MacOsCpuArchitecture.getCurrent(project).map { it.konanTarget },
        ): Configuration =
            project.configurations.maybeCreate(name).apply {
                isCanBeConsumed = false
                isCanBeResolved = true

                exclude("org.jetbrains.kotlin", "kotlin-stdlib")
                exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
                exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
                exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

                attributes {
                    attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
                    attributeProvider(KotlinNativeTarget.konanTargetAttribute, konanTarget)
                    attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
                }
            }
    }
}
