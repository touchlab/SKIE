package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import co.touchlab.skie.acceptancetests.framework.util.isCI
import io.kotest.assertions.print.print
import io.kotest.core.project.projectContext
import io.kotest.core.spec.style.scopes.FunSpecContainerScope

class ExternalLibrariesTestResultProcessor {

    suspend fun processResult(scope: FunSpecContainerScope, results: Map<ExternalLibraryTest, TestResultWithLogs>) {
        results.toList().sortedBy { (test, _) ->
            test.library.index
        }.forEach { (test, result) ->
            scope.test(test.library.fullName) {
                if (!isCI) {
                    println(result.logs)
                }

                test.expectedResult.shouldBe(result)

                this.projectContext.print()
            }
        }

        val failures = results.toList().filter { (test, result) ->
            !test.expectedResult.hasSucceeded(result)
        }

        if (failures.isNotEmpty()) {
            println("To run only failed tests add env variable:")
            println("onlyIndices=" + failures.joinToString(",") { "${it.first.library.index}" })
            println("${failures.size} failed out of ${results.size}")
        }
    }
}
