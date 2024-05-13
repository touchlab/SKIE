package co.touchlab.skie.test.suite.gradle.basic

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.template.Templates
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode
import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.assertEquals

@Suppress("ClassName")
@Smoke
@GradleTests
class KotlinBinaryDsl_XCFramework_SingleTargetTests: BaseGradleTests() {
    @MatrixTest
    fun `single target`(
        kotlinVersion: KotlinVersion,
        target: KotlinTarget.Native.Darwin,
        buildConfiguration: BuildConfiguration,
        linkMode: LinkMode,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                target(target)

                registerNativeFrameworks(kotlinVersion, buildConfiguration, linkMode, includeXcframework = true)
            }

            workaroundFatFrameworkConfigurationIfNeeded(kotlinVersion)
        }

        copyToCommonMain(Templates.basic)

        runGradle(
            arguments = arrayOf("assembleXCFramework"),
            assertResult = { result ->
                assertEquals(TaskOutcome.SUCCESS, result.task(":assembleXCFramework")?.outcome)
            },
        )

        val frameworkParentName = when (target) {
            KotlinTarget.Native.Ios.SimulatorArm64 -> "ios-arm64-simulator"
            KotlinTarget.Native.Ios.X64 -> "ios-x86_64-simulator"
            KotlinTarget.Native.Ios.Arm64 -> "ios-arm64"
            KotlinTarget.Native.Tvos.SimulatorArm64 -> "tvos-arm64-simulator"
            KotlinTarget.Native.Tvos.X64 -> "tvos-x86_64-simulator"
            KotlinTarget.Native.Tvos.Arm64 -> "tvos-arm64"
            KotlinTarget.Native.MacOS.Arm64 -> "macos-arm64"
            KotlinTarget.Native.MacOS.X64 -> "macos-x86_64"
        }

        buildSwift(
            target,
            Templates.basic,
            frameworkParentPath = "build/XCFrameworks/${buildConfiguration.name.lowercase()}/gradle_test.xcframework/$frameworkParentName",
        )

        if (target is KotlinTarget.Native.MacOS) {
            runSwift()
        }
    }
}
