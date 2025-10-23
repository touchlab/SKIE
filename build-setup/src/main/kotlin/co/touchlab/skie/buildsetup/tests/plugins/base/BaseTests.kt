@file:OptIn(ExternalKotlinTargetApi::class, ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.tests.plugins.base

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityBuildConfig
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.util.enquoted
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import co.touchlab.skie.gradle.util.implementation
import co.touchlab.skie.gradle.util.testImplementation
import co.touchlab.skie.gradle.util.withKotlinNativeCompilerEmbeddableDependency
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
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
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import java.io.File

abstract class BaseTests : Plugin<Project> {

    private val testInputProperties = listOf(
        "keepTemporaryFiles",
    )

    override fun apply(project: Project) = with(project) {
        apply<BaseKotlin>()
        apply<UtilityMinimumTargetKotlinVersion>()
        apply<UtilityBuildConfig>()
        apply<KotlinPluginWrapper>()

        val primaryKotlinVersion = KotlinToolingVersionProvider.getActiveKotlinToolingVersion(project).primaryVersion
        val testOutputDirectory = testDirectory(primaryKotlinVersion)

        val (testDependencies, exportedTestDependencies) = configureDependencies(primaryKotlinVersion)
        configureBuildConfig(testDependencies, exportedTestDependencies, testOutputDirectory)
        configureTestTask(testDependencies, exportedTestDependencies, testOutputDirectory)
    }

    private fun Project.configureDependencies(primaryKotlinVersion: KotlinToolingVersion): Pair<Configuration, Configuration> {
        val exportedTestDependencies = maybeCreateTestDependencyConfiguration("testExportedDependencies").apply {
            isTransitive = false
        }

        val testDependencies = maybeCreateTestDependencyConfiguration("testDependencies").apply {
            extendsFrom(exportedTestDependencies)
        }

        withKotlinNativeCompilerEmbeddableDependency(primaryKotlinVersion, isTarget = true) { dependency ->
            dependencies {
                testImplementation(dependency)
            }
        }

        dependencies {
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

    private fun Project.maybeCreateTestDependencyConfiguration(
        name: String,
    ): Configuration =
        configurations.maybeCreate(name).apply {
            isCanBeConsumed = false
            isCanBeResolved = true

            exclude("org.jetbrains.kotlin", "kotlin-stdlib")
            exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
            exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
            exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

            attributes {
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
                attributeProvider(KotlinNativeTarget.konanTargetAttribute, provider { MacOsCpuArchitecture.getCurrent().konanTarget })
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
            }
        }

    private fun Project.testDirectory(kotlinToolingVersion: KotlinToolingVersion): Provider<Directory> =
        project.layout.buildDirectory.map { it.dir(kotlinToolingVersion.toString()) }

    private fun Collection<File>.toListString(): String =
        this.joinToString(", ") { it.absolutePath.enquoted() }
}
