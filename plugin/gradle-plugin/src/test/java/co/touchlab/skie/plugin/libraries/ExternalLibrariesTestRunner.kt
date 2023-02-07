package co.touchlab.skie.plugin.libraries

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.channels.Channel
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildResultException
import java.io.File


class ExternalLibrariesTestRunner(
    private val testTmpDir: File,
    private val rootDir: File,
    private val testKitDir: File,
) {
    fun runTests(scope: FunSpec, tests: List<ExternalLibraryTest>) {
        val channel = Channel<ExternalLibraryTestResult>()
        scope.concurrency = 2

        scope.test("Evaluation") {
            val result = try {
                GradleRunner.create()
                    .withProjectDir(rootDir)
                    .withTestKitDir(testKitDir)
                    .withArguments(
                        "linkDebugFrameworkIosArm64",
                        "--stacktrace",
                        "--continue",
                        "-Dorg.gradle.jvmargs=-Xmx20g",
                        "-Dorg.gradle.parallel=true",
                    )
                    .withPluginClasspath()
                    .build()
            } catch (e: UnexpectedBuildResultException) {
                e.buildResult
            }

            val testResult = ExternalLibraryTestResult(
                result,
                tests.map { test ->
                    test to (result.task(":${test.directoryName}:linkDebugFrameworkIosArm64")?.outcome ?: TaskOutcome.SKIPPED)
                }
            )

            channel.send(testResult)
            channel.close()
        }

        scope.context("Results") {
            val testResult = channel.receive()
            val resultProcessor = ExternalLibrariesTestResultProcessor(testTmpDir = testTmpDir, rootDir = rootDir)
            resultProcessor.processResult(this, testResult)
        }
    }

}
