package co.touchlab.skie.acceptancetests.framework

import kotlin.time.DurationUnit

class TestResultReporter(
    allTests: List<Test>,
) {

    private val totalTestsCount = allTests.size

    private val remainingTests = allTests.toMutableSet()

    private val successfulTests = mutableSetOf<Test>()

    private val failedTests = mutableSetOf<Test>()

    @Synchronized
    fun reportResult(test: Test, testResultWithLogs: TestResultWithLogs) {
        registerTestResult(test, testResultWithLogs)

        if (test.expectedResult.hasSucceeded(testResultWithLogs)) {
            print("\u001b[32m")
        } else {
            print("\u001b[31m")
        }

        print(testResultLine(test, testResultWithLogs))

        println("\u001b[0m")
    }

    @Synchronized
    fun testResultLine(test: Test, testResultWithLogs: TestResultWithLogs): String {
        registerTestResult(test, testResultWithLogs)

        return testResultLineForKnownResult(test, testResultWithLogs)
    }

    private fun registerTestResult(test: Test, result: TestResultWithLogs) {
        remainingTests.remove(test)

        if (test.expectedResult.hasSucceeded(result)) {
            successfulTests.add(test)
        } else {
            failedTests.add(test)
        }
    }

    private fun testResultLineForKnownResult(test: Test, testResultWithLogs: TestResultWithLogs): String {
        val formatedDuration = testResultWithLogs.duration.toString(DurationUnit.SECONDS, 2)

        val result = testResultWithLogs.hasSucceededAsString(test)

        val successCount = successfulTests.size
        val failedCount = failedTests.size
        val remainingCount = remainingTests.size

        return "${test.fullName}: $result [s:$successCount,f:$failedCount,r:$remainingCount,t:$totalTestsCount] (took $formatedDuration)"
    }
}
