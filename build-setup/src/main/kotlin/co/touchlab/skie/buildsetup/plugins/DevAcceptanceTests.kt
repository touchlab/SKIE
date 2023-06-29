package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import co.touchlab.skie.gradle.util.String.enquoted
import co.touchlab.skie.gradle.version.*
import co.touchlab.skie.gradle.version.target.*
import co.touchlab.skie.gradle.version.target.Target
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.File

abstract class DevAcceptanceTests: Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<OptInExperimentalCompilerApi>()
        apply<DevBuildconfig>()

        configureExpectedBuildConfig()

        val latestKotlin  = kotlinToolingVersionDimension().latest
        acceptanceTestsDimension().components.forEach { testType ->
            tasks.register("${testType.value}__kgp_latestTest") {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                dependsOn(tasks.named("${testType.value}__kgp_${latestKotlin.value}Test"))
            }
        }

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(acceptanceTestsDimension(), kotlinToolingVersionDimension()) { target ->
                val acceptanceTestType = target.acceptanceTest
                val kotlinTarget = jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                        attribute(Attribute.of("co.touchlab.skie.dev.acceptance-test", String::class.java), acceptanceTestType.value)
                    }
                }

                val exportedTestDependencies = createTestDependencyConfiguration(target, "AcceptanceTestExportedDependencies").apply {
                    isTransitive = false
                }
                val testDependencies = createTestDependencyConfiguration(target, "AcceptanceTestDependencies").apply {
                    extendsFrom(exportedTestDependencies)
                }
                dependencies {
                    testDependencies(project(":common:configuration:configuration-annotations"))
                    exportedTestDependencies(project(":runtime:runtime-kotlin"))

                    testDependencies(project(":acceptance-tests:test-dependencies:regular-dependency"))
                    exportedTestDependencies(project(":acceptance-tests:test-dependencies:exported-dependency"))
                }

                kotlinTarget.testRuns.configureEach {
                    executionTask.configure {
                        dependsOn(
                            testDependencies.buildDependencies,
                            exportedTestDependencies.buildDependencies,
                        )

                        maxHeapSize = "12g"

                        testLogging {
                            showStandardStreams = true
                        }
                    }
                }

                configureActualBuildConfig(
                    target = target,
                    acceptanceTestType = acceptanceTestType,
                    testDependencies = testDependencies,
                    exportedTestDependencies = exportedTestDependencies,
                    kotlinTarget = kotlinTarget,
                )

                kotlinTarget
            }

            configureSourceSet { sourceSet ->
                val kotlinVersion = sourceSet.kotlinToolingVersion.value

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                    weak("org.jetbrains.kotlin:kotlin-native-compiler-embeddable:$kotlinVersion")

                    testOnly("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
                }
            }
        }
    }

    private fun Project.configureExpectedBuildConfig() {
        extensions.configure<BuildConfigExtension> {
            generator(ExpectActualBuildConfigGenerator(isActualImplementation = false))

            sourceSets.named("test") {
                buildConfigField(
                    type = "String",
                    name = "TEST_RESOURCES",
                    value = "",
                )
                buildConfigField(
                    type = "String",
                    name = "BUILD",
                    value = "",
                )
                buildConfigField(
                    type = "co.touchlab.skie.acceptancetests.util.StringArray",
                    name = "DEPENDENCIES",
                    value = "",
                )
                buildConfigField(
                    type = "co.touchlab.skie.acceptancetests.util.StringArray",
                    name = "EXPORTED_DEPENDENCIES",
                    value = "",
                )
            }
        }
    }

    private fun Project.configureActualBuildConfig(
        target: Target,
        testDependencies: Configuration,
        exportedTestDependencies: Configuration,
        acceptanceTestType: AcceptanceTestsComponent,
        kotlinTarget: KotlinJvmTarget,
    ) {
        extensions.configure<BuildConfigExtension> {
            sourceSets.named(target.name + "Test").configure {
                generator(ExpectActualBuildConfigGenerator(isActualImplementation = true))
                className.set("TestBuildConfig")

                fun Collection<File>.toListString(): String =
                    this.joinToString(", ") { it.absolutePath.enquoted() }

                val resolvedDependencies = provider { testDependencies.resolve() }
                val exportedDependencies = provider { exportedTestDependencies.resolve() }

                buildConfigField(
                    type = "String",
                    name = "TEST_RESOURCES",
                    value = kotlinTarget.compilations.named("test").flatMap {
                        tasks.named<ProcessResources>(it.processResourcesTaskName)
                    }.map {
                        it.destinationDir.absolutePath.enquoted()
                    },
                )
                buildConfigField(
                    type = "String",
                    name = "BUILD",
                    value = layout.buildDirectory.map { it.dir(acceptanceTestType.value).asFile.absolutePath.enquoted() },
                )
                buildConfigField(
                    type = "co.touchlab.skie.acceptancetests.util.StringArray",
                    name = "DEPENDENCIES",
                    value = resolvedDependencies.map { "arrayOf(${it.toListString()})" },
                )
                buildConfigField(
                    type = "co.touchlab.skie.acceptancetests.util.StringArray",
                    name = "EXPORTED_DEPENDENCIES",
                    value = exportedDependencies.map { "arrayOf(${it.toListString()})" },
                )
            }
        }
    }

    private fun Project.createTestDependencyConfiguration(target: Target, name: String): Configuration {
        return configurations.create(target.name + name.replaceFirstChar { it.uppercase() }) {
            isCanBeConsumed = false
            isCanBeResolved = true

            exclude("org.jetbrains.kotlin", "kotlin-stdlib")
            exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

            attributes {
                attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
                attribute(KotlinNativeTarget.konanTargetAttribute, MacOsCpuArchitecture.getCurrent().konanTarget)
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
            }
        }
    }
}
