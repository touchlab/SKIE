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

@Suppress("ClassName")
@Smoke
@GradleTests
class KotlinArtifactDsl_XCFramework_SingleTargetTests: BaseGradleTests() {
    @MatrixTest
    fun `single target`(
        kotlinVersion: KotlinVersion,
        target: KotlinTarget.Native.Darwin,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                target(target)

                includeCoroutinesDependency()
            }

            kotlinArtifacts {
                xcframework(
                    kotlinVersion = kotlinVersion,
                    targets = listOf(target),
                    linkMode = linkMode,
                    buildConfiguration = configuration,
                )
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle()

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
            frameworkParentPath = "build/out/xcframework/${configuration.name.lowercase()}/gradle_test.xcframework/$frameworkParentName",
        )

        if (target is KotlinTarget.Native.MacOS) {
            runSwift()
        }
    }
}
