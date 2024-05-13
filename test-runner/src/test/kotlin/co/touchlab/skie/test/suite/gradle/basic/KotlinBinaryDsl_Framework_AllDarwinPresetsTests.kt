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
class KotlinBinaryDsl_Framework_AllDarwinPresetsTests: BaseGradleTests() {
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
            buildSwift(target, Templates.basic, builtFrameworkParentDir(target, buildConfiguration, isArtifactDsl = false))

            if (target is KotlinTarget.Native.MacOS) {
                runSwift()
            }
        }
    }
}
