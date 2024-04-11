package co.touchlab.skie.test.suite.gradle.basic

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.runner.SkieTestRunnerConfiguration
import co.touchlab.skie.test.template.Templates
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.exhaustive.exhaustive
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome

object Properties {
    val kotlinVersions = SkieTestRunnerConfiguration.kotlinVersions.exhaustive()
}

fun BuildTask?.shouldBeSuccess() {
    this.shouldNotBeNull()
        .outcome shouldBe TaskOutcome.SUCCESS
}

fun BuildResult.shouldBeSuccess() {
    this.tasks(TaskOutcome.FAILED).shouldBeEmpty()
}

interface ParallelDynamicTest {
    fun beforeEach() {}

    fun afterEach() {}
}

// object Source_BasicGradleTests {
//     fun `empty project`(kotlinVersion: KotlinVersion) {
//         tempTestDir.createFile(...).appendText(...)
//     }
// }
//
// class Kotlin1_8_0_BasicGradleTests_empty_project {
//     @Test
//     fun doTest() {
//         Source_BasicGradleTests.`empty project`(KotlinVersion("1.8.0"))
//     }
// }

@Suppress("ClassName")
@GradleTests
class BasicGradle_AllDarwinPresets: BaseGradleTests() {
    @Smoke
    @MatrixTest
    fun `basic project, all darwin presets`(
        kotlinVersion: KotlinVersion,
        buildConfiguration: BuildConfiguration,
        linkMode: LinkMode,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                targets(KotlinTarget.Native.Darwin)

                registerNativeFrameworks(kotlinVersion, buildConfiguration, linkMode)
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        KotlinTarget.Native.Darwin.targets.forEach { target ->
            buildSwift(target, Templates.basic, buildConfiguration)
        }
    }
}
