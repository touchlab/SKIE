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
import co.touchlab.skie.test.util.presets

@Suppress("ClassName")
@Smoke
@GradleTests
class KotlinArtifactDsl_UniversalFramework_AllDarwinTargetsTests: BaseGradleTests() {
    @MatrixTest
    fun `single darwin preset and single universal framework artifact`(
        kotlinVersion: KotlinVersion,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
        preset: KotlinTarget.Preset.Native.Darwin,
    ) {
        // Universal frameworks require unique architectures, so we need to ignore Arm64 simulators.
        val targetsWithoutArmSimulators = preset.targets.filter { target ->
            when (target) {
                KotlinTarget.Native.Ios.SimulatorArm64 -> false
                KotlinTarget.Native.Tvos.SimulatorArm64 -> false

                KotlinTarget.Native.Ios.X64,
                KotlinTarget.Native.Ios.Arm64,
                KotlinTarget.Native.MacOS.X64,
                KotlinTarget.Native.MacOS.Arm64,
                KotlinTarget.Native.Tvos.X64,
                KotlinTarget.Native.Tvos.Arm64 -> true
            }
        }

        rootBuildFile(kotlinVersion) {
            kotlin {
                allDarwin()

                includeCoroutinesDependency()
            }

            kotlinArtifacts {
                universalFramework(
                    kotlinVersion = kotlinVersion,
                    targets = targetsWithoutArmSimulators,
                    linkMode = linkMode,
                    buildConfiguration = configuration,
                )
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        targetsWithoutArmSimulators.forEach { target ->
            buildSwift(
                target = target,
                frameworkParentPath = "build/out/fatframework/${configuration.name.lowercase()}",
                template = Templates.basic,
            )

            if (target is KotlinTarget.Native.MacOS) {
                runSwift()
            }
        }
    }
}
