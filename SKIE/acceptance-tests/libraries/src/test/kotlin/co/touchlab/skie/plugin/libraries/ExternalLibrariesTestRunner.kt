package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.acceptancetests.framework.TestResultReporter
import co.touchlab.skie.acceptancetests.framework.TestResultWithLogs
import co.touchlab.skie.acceptancetests.framework.testDispatcher
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.plugin.libraries.dependencies.kotlin.KotlinDependencyProvider
import co.touchlab.skie.plugin.libraries.lockfile.LockfileUpdater
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.concurrency.TestExecutionMode
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

class ExternalLibrariesTestRunner(
    private val testFilter: TestFilter,
    private val skieConfigurationData: CompilerSkieConfigurationData?,
    private val scopeSuffix: String,
    private val lockfileUpdater: LockfileUpdater?,
) {

    @OptIn(ExperimentalKotest::class)
    fun runTests(scope: FunSpec, tests: List<ExternalLibraryTest>) {
        val channel = Channel<Map<ExternalLibraryTest, TestResultWithLogs>>()

        scope.testExecutionMode = TestExecutionMode.LimitedConcurrency(2)

        scope.test("Evaluation${scopeSuffix}") {
            try {
                val filteredTests = tests.filter { testFilter.shouldBeEvaluated(it) }

                val testResultReporter = TestResultReporter(filteredTests)

                val results = KotlinDependencyProvider().use { kotlinDependencyProvider ->
                    val runner = SingleLibraryTestRunner(skieConfigurationData, kotlinDependencyProvider, lockfileUpdater)

                    withContext(testDispatcher()) {
                        filteredTests
                            .map { async { it to runner.runTest(it, testResultReporter) } }
                            .awaitAll()
                            .toMap()
                    }
                }

                lockfileUpdater?.addSkippedLibraries(filteredTests, tests)

                channel.send(results)
            } finally {
                channel.close()
            }
        }

        scope.context("Results${scopeSuffix}") {
            val testResult = channel.receive()

            val resultProcessor = ExternalLibrariesTestResultProcessor()
            resultProcessor.processResult(this, testResult)

            lockfileUpdater?.writeToDisk()
        }
    }
}
