package co.touchlab.skie.acceptancetests.framework.internal.testrunner

import co.touchlab.skie.acceptancetests.framework.ExpectedTestResult
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.skie.acceptancetests.framework.TestNode
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestCompiler
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.KotlinTestLinker
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.PluginConfigurationGenerator
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftCodeEnhancer
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftProgramRunner
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.swift.SwiftTestCompiler
import java.nio.file.Path
import kotlin.io.path.writeText

internal class TestRunner(private val tempFileSystemFactory: TempFileSystemFactory) {

    fun runTest(test: TestNode.Test): TestResult {
        val tempFileSystem = tempFileSystemFactory.create(test)
        val testResultBuilder = TestResultBuilder()

        return IntermediateResult.Value(test.kotlinFiles)
            .flatMap { compileKotlin(it, tempFileSystem, testResultBuilder) }
            .zip { generateConfiguration(test.configFiles, tempFileSystem, testResultBuilder) }
            .flatMap { linkKotlin(it.first, it.second, tempFileSystem, testResultBuilder) }
            .pairWith { enhanceSwiftCode(test.swiftCode, tempFileSystem) }
            .flatMap { compileSwift(it.first, it.second, tempFileSystem, testResultBuilder) }
            .finalize { runSwift(it, testResultBuilder) }
            .also { writeResult(test, it) }
            .also { reportResult(test, it) }
    }

    private fun compileKotlin(
        kotlinFiles: List<Path>,
        tempFileSystem: TempFileSystem,
        testResultBuilder: TestResultBuilder,
    ): IntermediateResult<Path> =
        KotlinTestCompiler(tempFileSystem, testResultBuilder).compile(kotlinFiles)

    private fun generateConfiguration(
        configFiles: List<Path>,
        tempFileSystem: TempFileSystem,
        testResultBuilder: TestResultBuilder,
    ): IntermediateResult<Path> =
        PluginConfigurationGenerator(tempFileSystem, testResultBuilder).generate(configFiles)

    private fun linkKotlin(
        klib: Path,
        configuration: Path,
        tempFileSystem: TempFileSystem,
        testResultBuilder: TestResultBuilder,
    ): IntermediateResult<Path> =
        KotlinTestLinker(tempFileSystem, testResultBuilder).link(klib, configuration)

    private fun enhanceSwiftCode(swiftCode: String, tempFileSystem: TempFileSystem): Path =
        SwiftCodeEnhancer(tempFileSystem).enhance(swiftCode)

    private fun compileSwift(
        kotlinFramework: Path,
        swiftFile: Path,
        tempFileSystem: TempFileSystem,
        testResultBuilder: TestResultBuilder,
    ): IntermediateResult<Path> =
        SwiftTestCompiler(tempFileSystem, testResultBuilder).compile(kotlinFramework, swiftFile)

    private fun runSwift(binary: Path, testResultBuilder: TestResultBuilder): TestResult =
        SwiftProgramRunner(testResultBuilder).runProgram(binary)

    private fun writeResult(test: TestNode.Test, result: TestResult) {
        val resultAsText = test.expectedResult.hasSucceededAsString(result)

        test.resultPath.writeText(resultAsText)
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
