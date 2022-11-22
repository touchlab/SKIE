package co.touchlab.skie.acceptancetests

import co.touchlab.skie.acceptance_tests.BuildConfig
import co.touchlab.skie.acceptancetests.framework.AcceptanceTestsRunner
import co.touchlab.skie.acceptancetests.framework.CompilerConfiguration
import co.touchlab.skie.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.skie.acceptancetests.framework.TestFilter
import co.touchlab.skie.acceptancetests.framework.TestNode
import co.touchlab.skie.acceptancetests.framework.plus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import kotlin.io.path.Path

class AcceptanceTests : FunSpec({

    val testsDirectory = Path(BuildConfig.RESOURCES).resolve("tests")
    val tempDirectory = Path(BuildConfig.BUILD).resolve("test-temp")

    val compilerConfiguration = CompilerConfiguration(
        dependencies = BuildConfig.DEPENDENCIES.toList(),
        exportedDependencies = BuildConfig.EXPORTED_DEPENDENCIES.toList(),
    )

    val tests = TestNode(testsDirectory, tempDirectory, compilerConfiguration)

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
