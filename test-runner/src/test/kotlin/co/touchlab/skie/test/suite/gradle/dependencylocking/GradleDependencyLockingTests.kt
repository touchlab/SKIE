package co.touchlab.skie.test.suite.gradle.dependencylocking

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.template.Templates
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode

@Smoke
@GradleTests
class GradleDependencyLockingTests: BaseGradleTests() {

    @MatrixTest
    fun `framework builds with strict dependency locking`(
        kotlinVersion: KotlinVersion,
    ) {
        // The extra first-level lock constraints are observable with Gradle 9.5.0 but not the test suite's default Gradle 8.8.
        val gradleVersion = "9.5.0"
        val target = KotlinTarget.Native.MacOS.Arm64
        val buildConfiguration = BuildConfiguration.Debug

        rootBuildFile(kotlinVersion) {
            kotlin {
                target(target)

                includeCoroutinesDependency()

                registerNativeFrameworks(
                    kotlinVersion = kotlinVersion,
                    buildConfiguration = buildConfiguration,
                    linkMode = LinkMode.Dynamic,
                )
            }

            appendLines(
                """
                dependencyLocking {
                    lockAllConfigurations()
                    lockMode.set(org.gradle.api.artifacts.dsl.LockMode.STRICT)
                }
                """.trimIndent(),
            )
        }

        copyToCommonMain(Templates.basic)

        runGradle(
            gradleVersion = gradleVersion,
            arguments = arrayOf("build", "--write-locks"),
        )
        runGradle(gradleVersion = gradleVersion)

        buildSwift(
            target = target,
            template = Templates.basic,
            frameworkParentPath = builtFrameworkParentDir(
                target = target,
                configuration = buildConfiguration,
                isArtifactDsl = false,
            ),
        )
        runSwift()
    }
}
