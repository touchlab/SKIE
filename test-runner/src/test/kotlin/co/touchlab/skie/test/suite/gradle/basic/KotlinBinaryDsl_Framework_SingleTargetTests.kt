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
class KotlinBinaryDsl_Framework_SingleTargetTests: BaseGradleTests() {
    @MatrixTest
    fun `basic project, single target`(
        kotlinVersion: KotlinVersion,
        target: KotlinTarget.Native.Darwin,
        buildConfiguration: BuildConfiguration,
        linkMode: LinkMode,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                target(target)

                includeCoroutinesDependency()

                registerNativeFrameworks(kotlinVersion, buildConfiguration, linkMode)
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        buildSwift(target, Templates.basic, builtFrameworkParentDir(target, buildConfiguration, isArtifactDsl = false))

        if (target is KotlinTarget.Native.MacOS) {
            runSwift()
        }
    }
}
