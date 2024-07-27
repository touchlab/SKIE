@file:OptIn(ExternalKotlinTargetApi::class, ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.buildsetup.plugins.extensions.DevAcceptanceTestsExtension
import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.architecture.MacOsCpuArchitecture
import co.touchlab.skie.gradle.util.enquoted
import co.touchlab.skie.gradle.util.withKotlinNativeCompilerEmbeddableDependency
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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.project
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi
// import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.external.DecoratedExternalKotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinCompilationDescriptor
import org.jetbrains.kotlin.gradle.plugin.mpp.external.ExternalKotlinTargetDescriptor
import org.jetbrains.kotlin.gradle.plugin.mpp.external.createCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.external.createExternalKotlinTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin
import java.io.File

class MultiDimensionalJvmTarget(delegate: Delegate): DecoratedExternalKotlinTarget(delegate)/*, HasConfigurableKotlinCompilerOptions<KotlinJvmCompilerOptions>*/ {
    @Suppress("UNCHECKED_CAST")
    override val compilations: NamedDomainObjectContainer<MultiDimensionalJvmCompilation>
        get() = super.compilations as NamedDomainObjectContainer<MultiDimensionalJvmCompilation>

//     override val compilerOptions: KotlinJvmCompilerOptions
//         get() = super.compilerOptions as KotlinJvmCompilerOptions
}

class MultiDimensionalJvmCompilation(delegate: Delegate): DecoratedExternalKotlinCompilation(delegate) {

}

abstract class DevAcceptanceTests : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<OptInExperimentalCompilerApi>()
        apply<DevBuildconfig>()
        apply<SerializationGradleSubplugin>()

        val devAcceptanceTests = project.extensions.create<DevAcceptanceTestsExtension>("devAcceptanceTests")

        configureExpectedBuildConfig()

        val latestKotlin = kotlinToolingVersionDimension().latest
        tasks.register("kgp_latestTest") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            dependsOn(tasks.named("kgp_${latestKotlin.value}Test"))
        }

        extensions.configure<KotlinMultiplatformExtension>() {
            sourceSets.commonTest {
                dependencies {
                    implementation(project(":acceptance-tests:acceptance-tests-framework"))
                    implementation(project(":common:util"))
                }
            }
        }

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(kotlinToolingVersionDimension()) { target ->
//                 val acceptanceTestType = target.acceptanceTest
                val kotlinToolingVersion = target.kotlinToolingVersion
                val _kotlinTarget = createExternalKotlinTarget {
                    this.targetName = "_" + target.name
                    this.platformType = KotlinPlatformType.jvm
                    this.targetFactory = ExternalKotlinTargetDescriptor.TargetFactory(::MultiDimensionalJvmTarget)

                    this.configure { target ->
                        (target as KotlinTarget).attributes {
                            attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.value))
                        }
                    }
                }
                val _mainCompilation = _kotlinTarget.createCompilation {
                    compilationName = KotlinCompilation.MAIN_COMPILATION_NAME
                    compilationFactory = ExternalKotlinCompilationDescriptor.CompilationFactory(::MultiDimensionalJvmCompilation)
                    defaultSourceSet = sourceSets.maybeCreate(compilationName)
                }

                val kotlinTarget = jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.value))
                    }
                }

                val skieIosArm64KotlinRuntimeDependency = maybeCreateTestDependencyConfiguration(
                    target = target,
                    name = "SkieKotlinRuntimeDependency",
                    konanTarget = KonanTarget.IOS_ARM64.name,
                ).apply {
                    isTransitive = false
                }

                val exportedTestDependencies = maybeCreateTestDependencyConfiguration(target, "AcceptanceTestExportedDependencies").apply {
                    isTransitive = false
                }
                val testDependencies = maybeCreateTestDependencyConfiguration(target, "AcceptanceTestDependencies").apply {
                    extendsFrom(exportedTestDependencies)
                }
                dependencies {
                    testDependencies(project(":common:configuration:configuration-annotations"))
//                     testDependencies(project(":configuration_annotations_impl_2_0_0"))
                    skieIosArm64KotlinRuntimeDependency(project(":runtime:runtime-kotlin")) {
                        attributes {
                            attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.value))
                        }
                    }

                    exportedTestDependencies(project(":runtime:runtime-kotlin")) {
                        attributes {
                            attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion.value))
                        }
                    }
                }

                devAcceptanceTests.getTests(target).configureEach {
                    group = "verification"

                    useJUnitPlatform()

                    dependsOn(
                        testDependencies.buildDependencies,
                        exportedTestDependencies.buildDependencies,
                        skieIosArm64KotlinRuntimeDependency.buildDependencies,
                    )

                    inputs.property(
                        "failedOnly", System.getenv("failedOnly"),
                    ).optional(true)
                    inputs.property(
                        "acceptanceTest", System.getenv("acceptanceTest"),
                    ).optional(true)
                    inputs.property(
                        "libraryTest", System.getenv("libraryTest"),
                    ).optional(true)
                    inputs.property(
                        "onlyIndices", System.getenv("onlyIndices"),
                    ).optional(true)
                    inputs.property(
                        "kotlinLinkMode", System.getenv("KOTLIN_LINK_MODE"),
                    ).optional(true)
                    inputs.property(
                        "kotlinBuildConfiguration", System.getenv("KOTLIN_BUILD_CONFIGURATION"),
                    ).optional(true)
                    inputs.property(
                        "disableSkie", System.getenv("disableSkie"),
                    ).optional(true)
                    inputs.property(
                        "ignoreExpectedFailures", System.getenv("ignoreExpectedFailures"),
                    ).optional(true)
                    inputs.property(
                        "ignoreDependencyConstraints", System.getenv("ignoreDependencyConstraints"),
                    ).optional(true)
                    inputs.property(
                        "skipDependencyResolution", System.getenv("skipDependencyResolution"),
                    ).optional(true)
                    inputs.property(
                        "skipKotlinCompilation", System.getenv("skipKotlinCompilation"),
                    ).optional(true)
                    inputs.property(
                        "skipSwiftCompilation", System.getenv("skipSwiftCompilation"),
                    ).optional(true)
                    inputs.property(
                        "updateLockfile", System.getenv("updateLockfile"),
                    ).optional(true)
                    inputs.property(
                        "includeFailedTestsInLockfile", System.getenv("includeFailedTestsInLockfile"),
                    ).optional(true)
                    inputs.property(
                        "queryMavenCentral", System.getenv("queryMavenCentral"),
                    ).optional(true)
                    inputs.property(
                        "ignoreLockfile", System.getenv("ignoreLockfile"),
                    ).optional(true)
                    inputs.property(
                        "convertLibraryDependenciesToTests", System.getenv("convertLibraryDependenciesToTests"),
                    ).optional(true)
                    inputs.property(
                        "skipTestsInLockfile", System.getenv("skipTestsInLockfile"),
                    ).optional(true)
                    inputs.property(
                        "keepTemporaryFiles", System.getenv("keepTemporaryFiles"),
                    ).optional(true)

                    outputs.dir(
                        testDirectory(project, kotlinToolingVersion),
                    )

                    maxHeapSize = "12g"

                    testLogging {
                        showStandardStreams = true
                    }
                }

                kotlinTarget.testRuns.configureEach {
                    executionTask.configure {
                        devAcceptanceTests.getTests(target).add(this)
                    }
                }

                configureActualBuildConfig(
                    target = target,
                    testDependencies = testDependencies,
                    exportedTestDependencies = exportedTestDependencies,
                    skieIosArm64KotlinRuntimeDependency = skieIosArm64KotlinRuntimeDependency,
                    kotlinToolingVersion = kotlinToolingVersion,
                    kotlinTarget = kotlinTarget,
                )

                kotlinTarget
            }

            configureSourceSet { sourceSet ->
                val kotlinVersion = sourceSet.kotlinToolingVersion.primaryVersion

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

                    withKotlinNativeCompilerEmbeddableDependency(kotlinVersion, isTarget = sourceSet.isTarget) {
                        weak(it)
                    }

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
                    type = "co.touchlab.skie.util.StringArray",
                    name = "DEPENDENCIES",
                    value = "",
                )
                buildConfigField(
                    type = "co.touchlab.skie.util.StringArray",
                    name = "EXPORTED_DEPENDENCIES",
                    value = "",
                )
                buildConfigField(
                    type = "String",
                    name = "LIBRARY_TESTS_DEPENDENCY_RESOLVER_PATH",
                    value = "",
                )
                buildConfigField(
                    type = "String",
                    name = "SKIE_IOS_ARM64_KOTLIN_RUNTIME_KLIB_PATH",
                    value = "",
                )
            }
        }
    }

    private fun Project.configureActualBuildConfig(
        target: Target,
        testDependencies: Configuration,
        exportedTestDependencies: Configuration,
        skieIosArm64KotlinRuntimeDependency: Configuration,
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
                val skieIosArm64KotlinRuntimeKlib = provider {
                    println(skieIosArm64KotlinRuntimeDependency.resolve().joinToString("\n") { "- ${it.absolutePath}"})
                    skieIosArm64KotlinRuntimeDependency.resolve().single()
                }

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
                    value = testDirectory(project, kotlinToolingVersion)
                        .map { it.asFile.absolutePath.enquoted() },
                )
                buildConfigField(
                    type = "co.touchlab.skie.util.StringArray",
                    name = "DEPENDENCIES",
                    value = resolvedDependencies.map { "arrayOf(${it.toListString()})" },
                )
                buildConfigField(
                    type = "co.touchlab.skie.util.StringArray",
                    name = "EXPORTED_DEPENDENCIES",
                    value = exportedDependencies.map { "arrayOf(${it.toListString()})" },
                )
                buildConfigField(
                    type = "String",
                    name = "LIBRARY_TESTS_DEPENDENCY_RESOLVER_PATH",
                    value = layout.projectDirectory.dir("library-tests-dependency-resolver").asFile.absolutePath.enquoted(),
                )
                buildConfigField(
                    type = "String",
                    name = "SKIE_IOS_ARM64_KOTLIN_RUNTIME_KLIB_PATH",
                    value = skieIosArm64KotlinRuntimeKlib.map { it.absolutePath.enquoted() },
                )
            }
        }
    }

    private fun Project.maybeCreateTestDependencyConfiguration(target: Target, name: String): Configuration =
        maybeCreateTestDependencyConfiguration(
            target = target,
            name = name,
            konanTarget = MacOsCpuArchitecture.getCurrent().konanTarget,
        )

    private fun Project.maybeCreateTestDependencyConfiguration(
        target: Target,
        name: String,
        konanTarget: String,
    ): Configuration =
        configurations.maybeCreate(target.name + name.replaceFirstChar { it.uppercase() }).apply {
            isCanBeConsumed = false
            isCanBeResolved = true

            exclude("org.jetbrains.kotlin", "kotlin-stdlib")
            exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
            exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
            exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

            attributes {
                attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
                attributeProvider(KotlinNativeTarget.konanTargetAttribute, provider { konanTarget })
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, KotlinUsages.KOTLIN_API))
            }
        }

    companion object {

        fun testDirectory(
            project: Project,
            kotlinToolingVersion: KotlinToolingVersionComponent,
        ): Provider<Directory> {
            return project.layout.buildDirectory.map {
                it.dir(kotlinToolingVersion.value)
            }
        }
    }
}
