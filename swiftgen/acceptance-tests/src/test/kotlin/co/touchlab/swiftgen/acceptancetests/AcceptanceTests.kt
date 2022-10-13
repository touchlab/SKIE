package co.touchlab.swiftgen.acceptancetests

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.acceptancetests.framework.AcceptanceTestsRunner
import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.swiftgen.acceptancetests.framework.TestFilter
import co.touchlab.swiftgen.acceptancetests.framework.TestNode
import co.touchlab.swiftgen.acceptancetests.framework.plus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import kotlin.io.path.Path

class AcceptanceTests : FunSpec({

    val testsDirectory = Path(BuildConfig.RESOURCES).resolve("tests")
    val tempDirectory = Path(BuildConfig.BUILD).resolve("test-temp")

    val tests = TestNode(testsDirectory, tempDirectory)

    val tempFileSystemFactory = TempFileSystemFactory()
    val testFilter = buildTestFilter()

    val runner = AcceptanceTestsRunner(tempFileSystemFactory, testFilter)

    runner.runTests(this, tests)
}) {

    override fun isolationMode(): IsolationMode = IsolationMode.SingleInstance
}

private fun buildTestFilter(): TestFilter {
    var testFilter: TestFilter = TestFilter.Empty

    val regex = System.getenv("acceptanceTest")
    if (regex != null) {
        testFilter += TestFilter.Regex(regex)
    }

    if ("failedOnly" in System.getenv()) {
        testFilter += TestFilter.FailedOnly
    }

    return testFilter
}
