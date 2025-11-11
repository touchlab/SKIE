package co.touchlab.skie.buildsetup.tests.plugins.tests

import co.touchlab.skie.buildsetup.tests.extensions.LibraryTestsExtension
import co.touchlab.skie.buildsetup.tests.plugins.base.BaseTestsPlugin
import co.touchlab.skie.buildsetup.tests.tasks.CheckLibraryExpectedFailuresConsistencyTask
import co.touchlab.skie.buildsetup.util.TestProperties
import co.touchlab.skie.buildsetup.util.enquoted
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin

abstract class TestsLibraryTestsPlugin : Plugin<Project> {

    private val testInputProperties = listOf(
        "failedOnly",
        "libraryTest",
        "onlyIndices",
        "disableSkie",
        "ignoreExpectedFailures",
        "ignoreDependencyConstraints",
        "skipDependencyResolution",
        "skipKotlinCompilation",
        "skipSwiftCompilation",
        "updateLockfile",
        "updateLockfileIncrementally",
        "includeFailedTestsInLockfile",
        "queryMavenCentral",
        "queryMavenCentral-fromPage",
        "queryMavenCentral-numberOfPages",
        "ignoreLockfile",
        "convertLibraryDependenciesToTests",
        "skipTestsInLockfile",
        "onlyUnresolvedVersions",
    )

    override fun apply(project: Project) = with(project) {
        apply<BaseTestsPlugin>()
        apply<SerializationGradleSubplugin>()

        val activeKotlinVersionName = SupportedKotlinVersionProvider.getPrimaryKotlinVersion(project).name

        val extension = registerExtension(activeKotlinVersionName)

        val skieIosArm64KotlinRuntimeDependencyConfiguration = getSkieIosArm64KotlinRuntimeDependencyConfiguration()

        configureBuildConfig(activeKotlinVersionName, skieIosArm64KotlinRuntimeDependencyConfiguration)
        configureTestTask(skieIosArm64KotlinRuntimeDependencyConfiguration)

        val checkLibraryExpectedFailuresConsistencyTask = registerCheckLibraryExpectedFailuresConsistencyTask()

        configureTests(extension, checkLibraryExpectedFailuresConsistencyTask)
    }

    private fun Project.registerExtension(activeKotlinVersionName: KotlinToolingVersion): LibraryTestsExtension =
        extensions.create<LibraryTestsExtension>("libraryTests").apply {
            lockFile.set(
                layout.projectDirectory.file("${testVersionedResourcesPath(activeKotlinVersionName)}/libraries.lock"),
            )
        }

    private fun Project.getSkieIosArm64KotlinRuntimeDependencyConfiguration(): Configuration {
        val skieIosArm64KotlinRuntimeDependency = BaseTestsPlugin.maybeCreateTestDependencyConfiguration(
            project = project,
            name = "SkieKotlinRuntimeDependency",
            konanTarget = provider { KonanTarget.IOS_ARM64.name },
        ).apply {
            isTransitive = false
        }

        dependencies {
            skieIosArm64KotlinRuntimeDependency(project(":runtime:runtime-kotlin"))
        }

        return skieIosArm64KotlinRuntimeDependency
    }

    private fun Project.configureBuildConfig(activeKotlinVersionName: KotlinToolingVersion, skieIosArm64KotlinRuntimeDependency: Configuration) {
        val skieIosArm64KotlinRuntimeKlib = provider {
            skieIosArm64KotlinRuntimeDependency.resolve().single()
        }

        extensions.configure<BuildConfigExtension> {
            buildConfigField(
                type = "String",
                name = "EXPECTED_FAILURES_PATH",
                value = layout.projectDirectory.dir("${testVersionedResourcesPath(activeKotlinVersionName)}/expected-failures").asFile.absolutePath,
            )

            buildConfigField(
                type = "String",
                name = "LOCKFILE_PATH",
                value = layout.projectDirectory.dir("${testVersionedResourcesPath(activeKotlinVersionName)}/libraries.lock").asFile.absolutePath,
            )

            buildConfigField(
                type = "String",
                name = "LIBRARY_TESTS_DEPENDENCY_RESOLVER_PATH",
                value = layout.projectDirectory.dir("library-tests-dependency-resolver").asFile.absolutePath,
            )

            buildConfigField(
                type = "String",
                name = "SKIE_IOS_ARM64_KOTLIN_RUNTIME_KLIB_PATH",
                value = skieIosArm64KotlinRuntimeKlib.map { it.absolutePath.enquoted() },
            )
        }
    }

    private fun Project.configureTestTask(skieIosArm64KotlinRuntimeDependency: Configuration) {
        tasks.named("test", Test::class.java).configure {
            testInputProperties.forEach {
                inputs.property(it, provider { TestProperties[it] }).optional(true)
            }

            dependsOn(
                skieIosArm64KotlinRuntimeDependency.buildDependencies,
            )
        }
    }

    private fun Project.registerCheckLibraryExpectedFailuresConsistencyTask(): TaskProvider<out Task> =
        tasks.register<CheckLibraryExpectedFailuresConsistencyTask>("checkExpectedFailuresConsistency") {
            val expectedFailuresFiles = layout.projectDirectory.asFile.resolve("src/test/resources/tests")
                .listFiles()
                .sortedBy { it.name }
                .mapNotNull { it.resolve("expected-failures").takeIf { file -> file.exists() } }

            val supportedVersions = SupportedKotlinVersionProvider.getSupportedKotlinVersions(project)

            this.expectedFailuresFiles.from(expectedFailuresFiles)
            this.supportedKotlinVersions.set(supportedVersions)
        }

    private fun Project.configureTests(
        extension: LibraryTestsExtension,
        checkExpectedFailuresConsistencyTask: TaskProvider<out Task>,
    ) {
        val mainTestTask = tasks.named<Test>("test")

        mainTestTask.configure {
            dependsOn(checkExpectedFailuresConsistencyTask)
        }

        extension.tests.configureEach {
            val test = this

            val task = tasks.register<Test>(test.name) {
                group = "verification"
                description = test.description.get()

                maxHeapSize = "12g"

                testClassesDirs = mainTestTask.get().testClassesDirs
                classpath = mainTestTask.get().classpath
            }

            afterEvaluate {
                task.configure {
                    test.systemProperties.finalizeValue()
                    test.systemFlags.finalizeValue()

                    test.systemProperties.get().forEach { (key, value) ->
                        systemProperty(key, value)
                    }

                    test.systemFlags.get().forEach { systemFlag ->
                        systemProperty(systemFlag, "")
                    }
                }
            }
        }
    }

    private fun testVersionedResourcesPath(activeKotlinVersionName: KotlinToolingVersion): String =
        "src/test/resources/tests/${activeKotlinVersionName}"
}
