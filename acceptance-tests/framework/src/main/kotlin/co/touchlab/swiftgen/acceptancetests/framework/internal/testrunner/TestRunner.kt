package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.acceptancetests.framework.ExpectedTestResult
import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.swiftgen.acceptancetests.framework.TestNode
import co.touchlab.swiftgen.acceptancetests.framework.TestResult
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinCodeEnhancer
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.kotlin.PluginConfigurationGenerator
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.swift.SwiftCodeEnhancer
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.swift.SwiftProgramRunner
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.swift.SwiftTestCompiler
import java.nio.file.Path
import kotlin.io.path.writeText

internal class TestRunner(private val tempFileSystemFactory: TempFileSystemFactory) {

    fun runTest(test: TestNode.Test): TestResult {
        val tempFileSystem = tempFileSystemFactory.create(test)
        val testResultBuilder = TestResultBuilder()

        return IntermediateResult.Value(test.kotlinFiles)
            .map { enhanceKotlinCode(it) }
            .zip { generateConfiguration(test.configFiles, tempFileSystem, testResultBuilder) }
            .flatMap { compileKotlin(it.first, it.second, tempFileSystem, testResultBuilder) }
            .map { enhanceSwiftCode(test.swiftCode, tempFileSystem) to it }
            .flatMap { compileSwift(it.first, it.second, tempFileSystem, testResultBuilder) }
            .finalize { runSwift(it, testResultBuilder) }
            .also { writeResult(test, it) }
            .also { reportResult(test, it) }
    }

    private fun enhanceKotlinCode(kotlinFiles: List<Path>): List<Path> =
        KotlinCodeEnhancer().enhance(kotlinFiles)

    private fun generateConfiguration(
        configFiles: List<Path>,
        tempFileSystem: TempFileSystem,
        testResultBuilder: TestResultBuilder,
    ): IntermediateResult<Path> =
        PluginConfigurationGenerator(tempFileSystem, testResultBuilder).generate(configFiles)

    private fun compileKotlin(
        kotlinFiles: List<Path>,
        configuration: Path,
        tempFileSystem: TempFileSystem,
        testResultBuilder: TestResultBuilder,
    ): IntermediateResult<Path> =
        KotlinTestCompiler(tempFileSystem, testResultBuilder).compile(kotlinFiles, configuration)

    private fun enhanceSwiftCode(swiftCode: String, tempFileSystem: TempFileSystem): Path =
        SwiftCodeEnhancer(tempFileSystem).enhance(swiftCode)

    private fun compileSwift(
        swiftFile: Path,
        kotlinFrameworkDirectory: Path,
        tempFileSystem: TempFileSystem,
        testResultBuilder: TestResultBuilder,
    ): IntermediateResult<Path> =
        SwiftTestCompiler(tempFileSystem, testResultBuilder).compile(swiftFile, kotlinFrameworkDirectory)

    private fun runSwift(binary: Path, testResultBuilder: TestResultBuilder): TestResult =
        SwiftProgramRunner(testResultBuilder).runProgram(binary)

    private fun writeResult(test: TestNode.Test, result: TestResult) {
        val resultFile = test.resultPath(tempFileSystemFactory)

        val resultAsText = test.expectedResult.hasSucceededAsString(result)

        resultFile.writeText(resultAsText)
    }

    private fun reportResult(test: TestNode.Test, result: TestResult) {
        if (test.expectedResult.hasSucceeded(result)) {
            print("\u001b[32m")
        } else {
            print("\u001b[31m")
        }

        print("${test.fullName}: ${test.expectedResult.hasSucceededAsString(result)}")

        println("\u001b[0m")
    }

    private fun ExpectedTestResult.hasSucceededAsString(result: TestResult): String =
        if (this.hasSucceeded(result)) ExpectedTestResult.SUCCESS else ExpectedTestResult.FAILURE
}