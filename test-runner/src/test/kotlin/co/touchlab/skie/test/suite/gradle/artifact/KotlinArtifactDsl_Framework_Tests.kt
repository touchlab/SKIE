package co.touchlab.skie.test.suite.gradle.artifact

import co.touchlab.skie.test.*
import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.OnlyDebug
import co.touchlab.skie.test.annotation.filter.OnlyDynamic
import co.touchlab.skie.test.annotation.filter.OnlyFor
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.template.Templates
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode
import co.touchlab.skie.test.util.RawKotlinTarget

@OnlyFor(
    targets = [RawKotlinTarget.iosSimulatorArm64],
)
@OnlyDebug
@OnlyDynamic
@Suppress("ClassName")
@Smoke
@GradleTests
class KotlinArtifactDsl_Framework_Tests: BaseGradleTests() {

    @MatrixTest
    fun `single target and single framework artifact`(
        kotlinVersion: KotlinVersion,
        target: KotlinTarget.Native.Darwin,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                target(target)
            }

            +"""
                kotlinArtifacts {
                    Native.Framework {
                        target = ${target.id}
                        modes(${configuration.toString().uppercase()})
                        isStatic = ${linkMode.isStatic}
                    }
                }
            """.trimIndent()
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        buildSwift(target, Templates.basic, configuration)

        if (target is KotlinTarget.Native.MacOS) {
            runSwift()
        }
    }

    @MatrixTest
    fun `all darwin targets and single framework artifact`(
        kotlinVersion: KotlinVersion,
        target: KotlinTarget.Native.Darwin,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                allDarwin()
            }

            +"""
                kotlinArtifacts {
                    Native.Framework {
                        target = ${target.id}
                        modes(${configuration.toString().uppercase()})
                        isStatic = ${linkMode.isStatic}
                    }
                }
            """.trimIndent()
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        buildSwift(
            target = target,
            frameworkParentPath =  "build/out/framework/ios_simulator_arm64/debug",
            template = Templates.basic,
            configuration = configuration,
        )

        if (target is KotlinTarget.Native.MacOS) {
            runSwift()
        }
    }

    @MatrixTest
    fun `all darwin targets and all framework artifacts`(
        kotlinVersion: KotlinVersion,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                allDarwin()
            }

            +"kotlinArtifacts {"
            KotlinTarget.Native.Darwin.targets.forEach { target ->
                +"""
                |    Native.Framework {
                |        target = ${target.id}
                |        modes(${configuration.toString().uppercase()})
                |        isStatic = ${linkMode.isStatic}
                |    }
                """.trimMargin()
            }
            +"}"
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        KotlinTarget.Native.Darwin.targets.forEach { target ->
            buildSwift(target, Templates.basic, configuration)

            if (target is KotlinTarget.Native.MacOS) {
                runSwift()
            }
        }
    }
}
