package co.touchlab.skie.test.suite.gradle.configurationcache

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.OnlyConfigurationCache
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode
import co.touchlab.skie.test.util.StringBuilderScope
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.assertEquals

@Suppress("ClassName")
@Smoke
@GradleTests
@OnlyConfigurationCache
class GradleConfigurationCacheTests: BaseGradleTests() {

    // We don't need to run the whole build to check configuration cache
    private val gradleArguments = arrayOf("dependencies")
    private val assertGradleResult = { result: BuildResult ->
        assertEquals(TaskOutcome.SUCCESS, result.task(":dependencies")?.outcome)
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

                registerNativeFrameworks(
                    kotlinVersion = kotlinVersion,
                    buildConfiguration = configuration,
                    linkMode = linkMode,
                )
            }
        }

        runGradle(
            arguments = gradleArguments,
            assertResult = assertGradleResult,
        )
    }

    @MatrixTest
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

        runGradle(
            arguments = gradleArguments,
            assertResult = assertGradleResult,
        )
    }

    @MatrixTest
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

        runGradle(
            arguments = gradleArguments,
            assertResult = assertGradleResult,
        )
    }

    override fun StringBuilderScope.appendAdditionalGradleProperties() {
        +"org.gradle.configuration-cache=true\n"
    }

}
