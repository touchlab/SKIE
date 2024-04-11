package co.touchlab.skie.test.suite.gradle.artifact

import co.touchlab.skie.test.annotation.MatrixTest
import co.touchlab.skie.test.annotation.filter.Smoke
import co.touchlab.skie.test.annotation.type.GradleTests
import co.touchlab.skie.test.base.BaseGradleTests
import co.touchlab.skie.test.util.KotlinVersion

@Suppress("ClassName")
@Smoke
@GradleTests
class KotlinArtifactDsl_NoArtifact_Tests: BaseGradleTests() {

    @MatrixTest
    fun `no artifact`(kotlinVersion: KotlinVersion) {
        rootBuildFile(kotlinVersion) {
            kotlin {
                allIos()
            }
        }

        runGradle()
    }
}
