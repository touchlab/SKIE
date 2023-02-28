package co.touchlab.skie.plugin.libraries

import co.touchlab.skie.external_libraries.BuildConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import java.io.File

class ExternalLibrariesTest: FunSpec({
    System.setProperty("konan.home", BuildConfig.KONAN_HOME)

    val testTmpDir = File(System.getProperty("testTmpDir")).also {
        it.mkdirs()
    }

    val testLoader = ExternalLibrariesTestLoader(testTmpDir)
    val librariesToTest = testLoader.loadLibrariesToTest()

    val testRunner = ExternalLibrariesTestRunner(testTmpDir = testTmpDir, testFilter = buildTestFilter())
    testRunner.runTests(this, librariesToTest)
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.SingleInstance
}

private fun buildTestFilter(): TestFilter {
    var testFilter: TestFilter = TestFilter.Empty

    val regex = System.getenv("libraryTest")
    if (regex != null) {
        testFilter += TestFilter.Regex(regex)
    }

    val rawIndices = System.getenv("onlyIndices")
    if (rawIndices != null) {
        val indices = rawIndices.split(',').map { it.trim() }.filter { it.isNotBlank() }.map { it.toInt() }.toSet()
        testFilter += TestFilter.Indices(indices)
    }

    if ("failedOnly" in System.getenv()) {
        testFilter += TestFilter.FailedOnly
    }

    return testFilter
}
