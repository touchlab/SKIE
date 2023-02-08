package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import io.kotest.assertions.print.print
import io.kotest.core.project.projectContext
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe
import java.io.File

class ExternalLibrariesTestResultProcessor(
    private val testTmpDir: File,
) {
    suspend fun processResult(scope: FunSpecContainerScope, results: Map<ExternalLibraryTest, TestResultWithLogs>) {
        results.toList().sortedBy { (test, _) ->
            test.index
        }.forEach { (test, result) ->
            scope.test("[${test.index}]: ${test.library}") {
                println(result.logs)
                result.testResult shouldBe TestResult.Success

                this.projectContext.print()
            }
        }

        val failures = results.toList().filter { (_, result) ->
            result.testResult !is TestResult.Success
        }
        // TODO: Generate "fake tests" for each library, so that we can see which ones failed in the test report.
        if (failures.isNotEmpty()) {
            println("To run only failed tests:")
            println(failures.joinToString(", ") { "${it.first.index}" })
            println("${failures.size} failed out of ${results.size}")
        }
    }
}
