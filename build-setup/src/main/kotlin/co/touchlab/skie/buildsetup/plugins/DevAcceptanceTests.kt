package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import co.touchlab.skie.gradle.version.acceptanceTest
import co.touchlab.skie.gradle.version.acceptanceTestsDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.target.ExpectActualBuildConfigGenerator
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.File

abstract class DevAcceptanceTests: Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<OptInExperimentalCompilerApi>()
        apply<DevBuildconfig>()

        extensions.configure<BuildConfigExtension> {
            generator(ExpectActualBuildConfigGenerator(isActualImplementation = false))

            sourceSets.named("test") {
                buildConfigField(
                    type = "String",
                    name = "RESOURCES",
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

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(acceptanceTestsDimension(), kotlinToolingVersionDimension())

            createTarget { target ->
                val kotlinTarget = jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                        attribute(Attribute.of("co.touchlab.skie.dev.acceptance-test", String::class.java), target.acceptanceTest.value)
                    }
                }

                val acceptanceTestDependencies = configurations.create(target.name + "AcceptanceTestDependencies") {
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
                dependencies {
                    acceptanceTestDependencies(project(":common:configuration:configuration-annotations"))
                    acceptanceTestDependencies(project(":runtime:runtime-kotlin"))
                }

                kotlinTarget.testRuns.configureEach {
                    executionTask.configure {
                        dependsOn(acceptanceTestDependencies.buildDependencies)

                        maxHeapSize = "12g"

                        testLogging {
                            showStandardStreams = true
                        }
                    }
                }

                extensions.configure<BuildConfigExtension> {
                    sourceSets.named(target.name + "Test").configure {
                        generator(ExpectActualBuildConfigGenerator(isActualImplementation = true))
                        className.set("TestBuildConfig")

                        fun Collection<File>.toListString(): String =
                            this.joinToString(", ") { "\"${it.absolutePath}\"" }

                        val resolvedDependencies = provider { acceptanceTestDependencies.resolve() }
                        val exportedDependencies = provider { acceptanceTestDependencies.filter { it.path.contains("SKIE/runtime/kotlin") }.toList() }

                        buildConfigField(
                            type = "String",
                            name = "RESOURCES",
                            value = kotlinTarget.compilations.named("test").flatMap {
                                tasks.named<ProcessResources>(it.processResourcesTaskName)
                            }.map {
                                "\"${it.destinationDir.absolutePath}\""
                            },
                        )
                        buildConfigField(
                            type = "String",
                            name = "BUILD",
                            value = layout.buildDirectory.map { "\"${it.asFile.absolutePath}\"" },
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

                kotlinTarget
            }

            configureSourceSet { sourceSet ->
                val kotlinVersion = sourceSet.kotlinToolingVersion.value

                addPlatform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion")
                addWeakDependency("org.jetbrains.kotlin:kotlin-stdlib", configureVersion(kotlinVersion))
                addWeakDependency("org.jetbrains.kotlin:kotlin-native-compiler-embeddable", configureVersion(kotlinVersion))
            }
        }

//         tasks.withType<Test>() {
//             dependsOn(acceptanceTestDependencies.buildDependencies)
//
//             maxHeapSize = "12g"
//
//             testLogging {
//                 showStandardStreams = true
//             }
//         }
    }
}
