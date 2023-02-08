package co.touchlab.skie.acceptancetests.framework

import kotlin.time.Duration

data class TestResultWithLogs(
    val testResult: TestResult,
    val duration: Duration,
    val logs: String,
)
