package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.TestNode
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestResult
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal class TestRunner(private val tempFileSystem: TempFileSystem) {

    fun runTest(test: TestNode.Test): TestResult {
        val logger = Logger()

        return compileKotlin(test.kotlinFiles, logger)
            .map { appendExitCallCheck(test.swiftFile) to it }
            .flatMap { compileSwift(it.first, it.second, logger) }
            .finalize { runSwift(it, logger) }
    }

    private fun compileKotlin(kotlinFiles: List<Path>, logger: Logger): IntermediateResult<Path> =
        KotlinTestCompiler(tempFileSystem, logger).compile(kotlinFiles)

    private fun appendExitCallCheck(swiftFile: Path): Path {
        val swiftCopy = tempFileSystem.createFile(".swift")

        val swiftCode = swiftFile.readText()

        val modifiedSwiftCode = "$swiftCode\n\nfatalError(\"${TestResult.MissingExit.ERROR_MESSAGE}\")"

        swiftCopy.writeText(modifiedSwiftCode)

        return swiftCopy
    }

    private fun compileSwift(
        swiftFile: Path,
        kotlinFrameworkDirectory: Path,
        logger: Logger
    ): IntermediateResult<Path> =
        SwiftTestCompiler(tempFileSystem, logger).compile(swiftFile, kotlinFrameworkDirectory)

    private fun runSwift(binary: Path, logger: Logger): TestResult =
        SwiftProgramRunner(logger).runProgram(binary)
}