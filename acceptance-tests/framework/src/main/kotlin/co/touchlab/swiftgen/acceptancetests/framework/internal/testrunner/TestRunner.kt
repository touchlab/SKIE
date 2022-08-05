package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystemFactory
import co.touchlab.swiftgen.acceptancetests.framework.TestNode
import co.touchlab.swiftgen.acceptancetests.framework.TestResult
import java.nio.file.Path

internal class TestRunner(private val tempFileSystemFactory: TempFileSystemFactory) {

    fun runTest(test: TestNode.Test): TestResult {
        val tempFileSystem = tempFileSystemFactory.create(test)
        val testResultBuilder = TestResultBuilder()

        return compileKotlin(test.kotlinFiles, tempFileSystem, testResultBuilder)
            .map { enhanceSwiftCode(test.swiftCode, tempFileSystem) to it }
            .flatMap { compileSwift(it.first, it.second, tempFileSystem, testResultBuilder) }
            .finalize { runSwift(it, testResultBuilder) }
    }

    private fun compileKotlin(
        kotlinFiles: List<Path>,
        tempFileSystem: TempFileSystem,
        testResultBuilder: TestResultBuilder,
    ): IntermediateResult<Path> =
        KotlinTestCompiler(tempFileSystem, testResultBuilder).compile(kotlinFiles)

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
}