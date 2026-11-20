package co.touchlab.skie.test.suite.gradle.configurationcache

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.OnlyKotlinUpTo
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.template.Templates
import co.touchlab.skie.test.trait.gradle.BuildGradleBuilder
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode
import co.touchlab.skie.test.util.StringBuilderScope
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildFailure
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@Suppress("ClassName")
@Smoke
@GradleTests
class GradleConfigurationCacheTests: BaseGradleTests() {

    // We don't need to run the whole build to check configuration cache
    private val gradleArguments = arrayOf("assemble")
    private val assertGradleResult = { result: BuildResult ->
        assertEquals(TaskOutcome.SUCCESS, result.task(":assemble")?.outcome)
    }

    @MatrixTest
    fun `no artifact`(
        kotlinVersion: KotlinVersion,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                targets(KotlinTarget.Native)
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle(
            arguments = gradleArguments,
            assertResult = assertGradleResult,
        )
    }

    @MatrixTest
    fun `frameworks`(
        kotlinVersion: KotlinVersion,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                targets(KotlinTarget.Native)

                includeCoroutinesDependency()

                registerNativeFrameworks(
                    kotlinVersion = kotlinVersion,
                    buildConfiguration = configuration,
                    linkMode = linkMode,
                )
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle(
            arguments = gradleArguments,
            assertResult = assertGradleResult,
        )
    }

    @MatrixTest
    @OnlyKotlinUpTo(major = 2, minor = 2)
    fun `framework artifacts`(
        kotlinVersion: KotlinVersion,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                targets(KotlinTarget.Native)
            }

            kotlinArtifacts {
                KotlinTarget.Native.Darwin.targets.forEach { target ->
                    framework(
                        kotlinVersion = kotlinVersion,
                        target = target,
                        linkMode = linkMode,
                        buildConfiguration = configuration,
                    )
                }
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle(
            arguments = gradleArguments,
            assertResult = assertGradleResult,
        )
    }

    @MatrixTest
    @OnlyKotlinUpTo(major = 2, minor = 2)
    fun `xcframework artifact`(
        kotlinVersion: KotlinVersion,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                targets(KotlinTarget.Native)
            }

            kotlinArtifacts {
                xcframework(
                    kotlinVersion = kotlinVersion,
                    targets = KotlinTarget.Native.Darwin.targets,
                    linkMode = linkMode,
                    buildConfiguration = configuration,
                )
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle(
            arguments = gradleArguments,
            assertResult = assertGradleResult,
        )
    }

    // Regression test for the DefaultSourceDirectorySet serialization failure SKIE caused on the
    // link-framework graph: SwiftBundlingConfigurator.createSwiftSourceSet() built a live
    // SourceDirectorySet and let it reach the configuration cache, which cannot serialize it.
    //
    // The failure only surfaces when something puts SKIE's Kotlin-source-set container on a
    // serialization path. In the wild that is a consumer build action that captures the
    // KotlinNativeTarget (e.g. a doLast on the link task); a plain SKIE project never does, which is
    // why the `assemble` cases above — and even an unmodified link build — don't reproduce it. We
    // recreate that reachability here with a target-capturing doLast, then assert SKIE contributes no
    // SourceDirectorySet problem of its own. Unrelated KGP problems (its own `actualResources`
    // SourceDirectorySet and StoredPropertyStorage/ReferenceQueue) are expected to remain — they are
    // dragged in by the same capture and are not SKIE's to fix — so the assertion keys on SKIE's own
    // `createSwiftSourceSet` frame rather than the generic problem type.
    @MatrixTest
    fun `framework link does not put SKIE swift sources in the configuration cache`(
        kotlinVersion: KotlinVersion,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                targets(KotlinTarget.Native)

                includeCoroutinesDependency()

                registerNativeFrameworks(
                    kotlinVersion = kotlinVersion,
                    buildConfiguration = BuildConfiguration.Debug,
                    linkMode = LinkMode.Dynamic,
                )
            }

            // Capture the KotlinNativeTarget in a stored link-task action. This makes
            // target.binaries -> SKIE's KGP shims -> the swift source set reachable from the
            // configuration cache, reproducing the consumer condition under which the bug fired.
            appendLines(
                """
                kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
                    val capturedTarget = this
                    binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.Framework>().configureEach {
                        linkTaskProvider.get().doLast { println(capturedTarget.binaries.size) }
                    }
                }
                """.trimIndent(),
            )
        }

        // Templates.basic ships a bundled Swift source under src/commonMain/swift.
        copyToCommonMain(Templates.basic)

        // `--dry-run` keeps configuration (and the configuration cache store) but skips the native
        // link. The build is EXPECTED to fail here regardless of SKIE: the unrelated KGP problem
        // (ReferenceQueue in StoredPropertyStorage) is a hard serialization error that aborts the
        // store even under `--configuration-cache-problems=warn`. That failure is orthogonal to the
        // SKIE bug, so we tolerate it and assert on the configuration cache report, which Gradle
        // writes either way.
        val buildFailure = try {
            runGradle(
                arguments = arrayOf(
                    ":linkDebugFrameworkIosArm64",
                    "--dry-run",
                    "--configuration-cache-problems=warn",
                ),
                assertResult = null,
            )
            null
        } catch (failure: UnexpectedBuildFailure) {
            failure
        }

        val report = testProjectDir.walkTopDown()
            .firstOrNull { it.name == "configuration-cache-report.html" }
            ?.readText()

        if (report == null) {
            // No report means no configuration cache problems at all. If the build nonetheless
            // failed, it failed for some unrelated reason we should surface rather than swallow.
            buildFailure?.let { throw it }
            return
        }

        assertFalse(
            report.contains("createSwiftSourceSet"),
            "SKIE put its swift SourceDirectorySet on the configuration cache serialization path " +
                "(SwiftBundlingConfigurator.createSwiftSourceSet). It must expose the swift sources as a " +
                "serializable FileCollection instead.",
        )
    }

    // Regression test for project-dependency Swift missing from the framework on a clean build.
    // Project dependencies resolve to non-packed klib directories that only exist once the dependency
    // is compiled. UnpackSwiftSourcesTask used to hold the `isDirectory`-filtered linker klibs in a
    // ListProperty, whose value the configuration cache stored before the directories existed, which
    // dropped every project-dependency klib. The framework's bundled Swift calls the library's bundled
    // Swift, so the link only succeeds if the library's Swift was unpacked.
    @MatrixTest
    fun `framework link includes bundled Swift from project dependencies`(
        kotlinVersion: KotlinVersion,
    ) {
        settingsFile.appendText("\ninclude(\":library\")\n")

        val libraryBuildFile = BuildGradleBuilder(
            kotlinVersion = kotlinVersion,
            // The root project already puts both plugins on the classpath, so they can't be requested with a version.
            plugins = listOf(
                BuildGradleBuilder.Plugin(id = "org.jetbrains.kotlin.multiplatform", version = null),
                BuildGradleBuilder.Plugin(id = "co.touchlab.skie", version = null),
            ),
        ).apply {
            kotlin {
                target(KotlinTarget.Native.Ios.Arm64)
            }
        }

        testProjectDir.resolve("library/src/commonMain/kotlin").mkdirs()
        testProjectDir.resolve("library/build.gradle.kts")(libraryBuildFile)

        testProjectDir.resolve("library/src/commonMain/kotlin/Library.kt")("class Library\n")

        testProjectDir.resolve("library/src/commonMain/swift").mkdirs()
        testProjectDir.resolve("library/src/commonMain/swift/LibrarySwift.swift")(
            """
            public func libraryBundledSwiftFunction() {
            }
            """.trimIndent(),
        )

        rootBuildFile(kotlinVersion) {
            kotlin {
                target(KotlinTarget.Native.Ios.Arm64)

                "sourceSets.commonMain" {
                    "dependencies" {
                        +"implementation(project(\":library\"))"
                    }
                }

                registerNativeFrameworks(
                    kotlinVersion = kotlinVersion,
                    buildConfiguration = BuildConfiguration.Debug,
                    linkMode = LinkMode.Dynamic,
                )
            }
        }

        copyToCommonMain(Templates.basic)

        testProjectDir.resolve("src/commonMain/swift/FrameworkSwift.swift")(
            """
            public func frameworkBundledSwiftFunction() {
                libraryBundledSwiftFunction()
            }
            """.trimIndent(),
        )

        runGradle(
            arguments = arrayOf(":linkDebugFrameworkIosArm64"),
            assertResult = { result ->
                assertEquals(TaskOutcome.SUCCESS, result.task(":linkDebugFrameworkIosArm64")?.outcome)
            },
        )
    }

    override fun StringBuilderScope.appendAdditionalGradleProperties() {
        +"org.gradle.configuration-cache=true\n"
    }

}
