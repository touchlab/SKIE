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
class KotlinArtifactDsl_XCFramework_AllDarwinTargetsTests: BaseGradleTests() {

    @MatrixTest
    fun `all darwin targets and single xcframework artifact`(
        kotlinVersion: KotlinVersion,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                allDarwin()
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

        runGradle()

        KotlinTarget.Native.Darwin.targets.forEach { target ->
            val frameworkParentName = when (target) {
                KotlinTarget.Native.Ios.SimulatorArm64, KotlinTarget.Native.Ios.X64 -> "ios-arm64_x86_64-simulator"
                KotlinTarget.Native.Ios.Arm64 -> "ios-arm64"
                KotlinTarget.Native.Tvos.SimulatorArm64, KotlinTarget.Native.Tvos.X64 -> "tvos-arm64_x86_64-simulator"
                KotlinTarget.Native.Tvos.Arm64 -> "tvos-arm64"
                KotlinTarget.Native.MacOS.Arm64, KotlinTarget.Native.MacOS.X64 -> "macos-arm64_x86_64"
            }

            buildSwift(
                target = target,
                frameworkParentPath = "build/out/xcframework/${configuration.name.lowercase()}/gradle_test.xcframework/$frameworkParentName",
                template = Templates.basic,
            )

            if (target is KotlinTarget.Native.MacOS) {
                runSwift()
            }
        }
    }
}
