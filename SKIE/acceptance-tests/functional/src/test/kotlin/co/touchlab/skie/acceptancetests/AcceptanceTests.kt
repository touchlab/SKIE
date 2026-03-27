package co.touchlab.skie.acceptancetests

import co.touchlab.skie.acceptancetests.framework.AcceptanceTestsRunner
import co.touchlab.skie.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.skie.acceptancetests.framework.TestFilter
import co.touchlab.skie.acceptancetests.framework.TestNode
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.CompilerArgumentsProvider
import co.touchlab.skie.acceptancetests.framework.plus
import co.touchlab.skie.acceptancetests.framework.util.TestProperties
import co.touchlab.skie.functional.TestBuildConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import kotlin.io.path.Path

class AcceptanceTests : FunSpec(
    {
        val testsDirectory = Path(TestBuildConfig.TEST_RESOURCES).resolve("tests")
        val tempDirectory = Path(TestBuildConfig.BUILD).resolve("test-temp")

        val compilerArgumentsProvider = CompilerArgumentsProvider(
            dependencies = TestBuildConfig.DEPENDENCIES.toList(),
            exportedDependencies = TestBuildConfig.EXPORTED_DEPENDENCIES.toList(),
        )

        val tests = TestNode(testsDirectory, tempDirectory, compilerArgumentsProvider)

        val tempFileSystemFactory = TempFileSystemFactory()
        val testFilter = buildTestFilter()

        val runner = AcceptanceTestsRunner(tempFileSystemFactory, testFilter)

        runner.runTests(this, tests)
    },
) {

    override fun isolationMode(): IsolationMode = IsolationMode.SingleInstance
}

private fun buildTestFilter(): TestFilter {
    var testFilter: TestFilter = TestFilter.Empty

    val regex = TestProperties["acceptanceTest"]
    if (regex != null) {
        testFilter += TestFilter.Regex(regex)
    }

    if ("failedOnly" in TestProperties) {
        testFilter += TestFilter.FailedOnly
    }

    return testFilter
}
