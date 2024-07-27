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
class KotlinArtifactDsl_UniversalFramework_SingleTargetTests: BaseGradleTests() {
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
                universalFramework(
                    kotlinVersion = kotlinVersion,
                    targets = listOf(target),
                    linkMode = linkMode,
                    buildConfiguration = configuration,
                )
            }
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        buildSwift(
            target,
            Templates.basic,
            frameworkParentPath = "build/out/fatframework/${configuration.name.lowercase()}",
        )

        if (target is KotlinTarget.Native.MacOS) {
            runSwift()
        }
    }
}
