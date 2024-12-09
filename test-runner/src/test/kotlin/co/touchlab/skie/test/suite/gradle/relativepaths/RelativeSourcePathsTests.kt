package co.touchlab.skie.test.suite.gradle.relativepaths

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.runner.BuildConfiguration
import co.touchlab.skie.test.template.Templates
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion
import co.touchlab.skie.test.util.LinkMode
import co.touchlab.skie.test.util.execute
import kotlin.test.assertContains
import kotlin.test.assertEquals

@Smoke
@GradleTests
class RelativeSourcePathsTests: BaseGradleTests() {

    @MatrixTest
    fun `basic`(
        kotlinVersion: KotlinVersion,
        linkMode: LinkMode,
        configuration: BuildConfiguration,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                allIos()

                registerNativeFrameworks(
                    kotlinVersion = kotlinVersion,
                    buildConfiguration = configuration,
                    linkMode = linkMode,
                )
            }

            +"""
                skie {
                    build {
                        enableRelativeSourcePathsInDebugSymbols = true
                    }
                }
            """.trimIndent()
        }

        copyToCommonMain(Templates.basic)

        runGradle()

        KotlinTarget.Native.Ios.targets.forEach { target ->
            val frameworkDir = builtFrameworkParentDir(target, configuration, isArtifactDsl = false)
            val dwarfContainingBinary = when (linkMode) {
                LinkMode.Dynamic -> "$frameworkDir/gradle_test.framework.dSYM/Contents/Resources/DWARF/gradle_test"
                LinkMode.Static -> "$frameworkDir/gradle_test.framework/gradle_test"
            }

            val debugSources = debugSourcesOf(dwarfContainingBinary)
            val expectedSources = listOf(
                "./src/commonMain/kotlin/templates/basic/BasicSkieFeatures.kt",
                "./bundled/gradle-test/bundled.gradle-test.BundledSwift.swift",
                "./generated/GradleTest/GradleTest.BasicEnum.swift",
                "./generated/GradleTest/GradleTest.SealedClass.swift",
                "./generated/GradleTest/GradleTest.SealedInterface.swift",
                "./generated/Skie/Skie.Namespace.swift",
            )
            expectedSources.forEach {
                assertContains(debugSources, it)
            }
        }
    }

    private fun debugSourcesOf(dwarfContainingBinary: String): Set<String> {
        val command = listOf(
            "/usr/bin/dwarfdump",
            "--show-sources",
            dwarfContainingBinary,
        )

        val result = command.joinToString(" ").execute(testProjectDir)
        assertEquals(0, result.exitCode)
        return result.stdOut.lines().toSet()
    }

}
