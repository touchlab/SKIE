package co.touchlab.skie.plugin.libraries

import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

class ExternalLibrariesTestResultProcessor(
    private val testTmpDir: File,
    private val rootDir: File,
) {
    suspend fun processResult(scope: FunSpecContainerScope, result: ExternalLibraryTestResult) {
        testTmpDir.resolve("run.log").apply {
            writeText(result.gradle.output)
            println("Gradle Build Log: $absolutePath")
        }
        testTmpDir.resolve("library-outcomes.log").apply {
            writeText(result.outcomes.joinToString("\n") { (test, outcome) ->
                "[${outcome}] for ${test.library} (library-${test.index})"
            })
            println("Library Outcomes Log: $absolutePath")
        }
        val failureOutcomes = setOf(
            // Link task failed
            TaskOutcome.FAILED,
            // Probably a dependency task failed
            TaskOutcome.SKIPPED,
            // No source files, shouldn't happen
            TaskOutcome.NO_SOURCE,
        )
        val failures = result.outcomes.filter { (_, outcome) ->
            outcome in failureOutcomes
        }
        // TODO: Generate "fake tests" for each library, so that we can see which ones failed in the test report.
        if (failures.isNotEmpty()) {
            println("To run only failed tests:")
            println(failures.joinToString(", ") { "${it.first.index}" })
            println("${failures.size} failed out of ${result.outcomes.size}")
        }

        result.outcomes.forEach { (test, outcome) ->
            when (outcome) {
                TaskOutcome.SUCCESS, TaskOutcome.FAILED -> scope.test(test.library) {
                    val logFile = rootDir.resolve(test.directoryName).resolve("build/co.touchlab.skie/debugFramework/iosArm64/swiftlink.log")
                    if (logFile.exists()) {
                        println(logFile.readText())
                    }

                    outcome shouldBe TaskOutcome.SUCCESS
                }
                TaskOutcome.UP_TO_DATE, TaskOutcome.SKIPPED, TaskOutcome.FROM_CACHE, TaskOutcome.NO_SOURCE -> scope.xtest("$outcome - ${test.library}") { }
            }
        }
    }
}
