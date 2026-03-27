package co.touchlab.skie.acceptancetests.framework

interface Test {

    val fullName: String

    val expectedResult: ExpectedTestResult
}

fun TestResultWithLogs.hasSucceededAsString(test: Test): String =
    if (test.expectedResult.hasSucceeded(this)) ExpectedTestResult.SUCCESS else ExpectedTestResult.FAILURE
