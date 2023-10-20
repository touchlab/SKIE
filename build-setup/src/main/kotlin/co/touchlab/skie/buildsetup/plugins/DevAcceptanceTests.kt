package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import co.touchlab.skie.gradle.util.enquoted
import co.touchlab.skie.gradle.version.AcceptanceTestsComponent
import co.touchlab.skie.gradle.version.KotlinToolingVersionComponent
import co.touchlab.skie.gradle.version.acceptanceTest
import co.touchlab.skie.gradle.version.acceptanceTestsDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.target.ExpectActualBuildConfigGenerator
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import co.touchlab.skie.gradle.version.target.Target
import co.touchlab.skie.gradle.version.target.latest
import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.project
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin
import java.io.File

abstract class DevAcceptanceTests : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<OptInExperimentalCompilerApi>()
        apply<DevBuildconfig>()
        apply<SerializationGradleSubplugin>()

        configureExpectedBuildConfig()

        val latestKotlin = kotlinToolingVersionDimension().latest
        acceptanceTestsDimension().components.forEach { testType ->
            tasks.register("${testType.value}__kgp_latestTest") {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                dependsOn(tasks.named("${testType.value}__kgp_${latestKotlin.value}Test"))
            }
        }

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(acceptanceTestsDimension(), kotlinToolingVersionDimension()) { target ->
                val acceptanceTestType = target.acceptanceTest
                val kotlinToolingVersion = target.kotlinToolingVersion
                val kotlinTarget = jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.value))
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
                        inputs.property(
                            "failedOnly", System.getenv("failedOnly"),
                        ).optional(true)
                        inputs.property(
                            "acceptanceTest", System.getenv("acceptanceTest"),
                        ).optional(true)
                        inputs.property(
                            "kotlinLinkMode", System.getenv("KOTLIN_LINK_MODE"),
                        ).optional(true)
                        inputs.property(
                            "kotlinBuildConfiguration", System.getenv("KOTLIN_BUILD_CONFIGURATION"),
                        ).optional(true)
                        outputs.dir(
                            testDirectory(project, acceptanceTestType, kotlinToolingVersion),
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
                    kotlinToolingVersion = kotlinToolingVersion,
                    kotlinTarget = kotlinTarget,
                )

                kotlinTarget
            }

            configureSourceSet { sourceSet ->
                val kotlinVersion = sourceSet.kotlinToolingVersion.primaryVersion

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
        kotlinToolingVersion: KotlinToolingVersionComponent,
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
                    value = testDirectory(project, acceptanceTestType, kotlinToolingVersion)
                        .map { it.asFile.absolutePath.enquoted() },
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

    companion object {

        fun testDirectory(
            project: Project,
            testType: AcceptanceTestsComponent,
            kotlinToolingVersion: KotlinToolingVersionComponent,
        ): Provider<Directory> {
            return project.layout.buildDirectory.map {
                it.dir(testType.value).dir(kotlinToolingVersion.value)
            }
        }
    }
}
