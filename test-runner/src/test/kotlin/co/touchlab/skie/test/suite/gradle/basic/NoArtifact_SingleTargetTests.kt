package co.touchlab.skie.test.suite.gradle.basic

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.util.KotlinTarget
import co.touchlab.skie.test.util.KotlinVersion

@Suppress("ClassName")
@Smoke
@GradleTests
class NoArtifact_SingleTargetTests: BaseGradleTests() {
    @MatrixTest
    fun `no artifact`(
        kotlinVersion: KotlinVersion,
        kotlinTarget: KotlinTarget,
    ) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                target(kotlinTarget)
            }
        }

        runGradle()
    }
}
