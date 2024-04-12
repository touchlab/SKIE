package co.touchlab.skie.test.suite.gradle.basic

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.util.KotlinVersion

@Suppress("ClassName")
@Smoke
@GradleTests
class NoArtifact_AllDarwinTargetsTests: BaseGradleTests() {
    @MatrixTest
    fun `no artifact`(kotlinVersion: KotlinVersion) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                allDarwin()
            }
        }

        runGradle()
    }
}
