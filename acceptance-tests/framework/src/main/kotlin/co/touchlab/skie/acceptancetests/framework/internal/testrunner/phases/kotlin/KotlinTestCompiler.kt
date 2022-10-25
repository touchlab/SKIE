package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestResultBuilder
import org.jetbrains.kotlin.cli.bc.K2Native
import org.jetbrains.kotlin.cli.bc.K2NativeCompilerArguments
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class KotlinTestCompiler(
    private val tempFileSystem: TempFileSystem,
    private val testResultBuilder: TestResultBuilder,
) {

    fun compile(kotlinFiles: List<Path>): IntermediateResult<Path> {
        val tempDirectory = tempFileSystem.createDirectory("kotlin-compiler")
        val outputFile = tempFileSystem.createFile("kotlin.klib")

        val (messageCollector, outputStream) = createCompilerOutputStream()

        val arguments = createCompilerArguments(kotlinFiles, tempDirectory, outputFile)

        val result = K2Native().exec(messageCollector, Services.EMPTY, arguments)

        return interpretResult(result, outputFile, outputStream)
    }

    private fun createCompilerOutputStream(): Pair<PrintingMessageCollector, OutputStream> {
        val outputStream = ByteArrayOutputStream()

        val messageCollector = PrintingMessageCollector(
            PrintStream(outputStream),
            MessageRenderer.GRADLE_STYLE,
            false,
        )

        return messageCollector to outputStream
    }

    private fun createCompilerArguments(
        kotlinFiles: List<Path>,
        tempDirectory: Path,
        outputFile: Path,
    ): K2NativeCompilerArguments =
        K2NativeCompilerArguments().apply {
            freeArgs = kotlinFiles.map { it.absolutePathString() }

            memoryModel = "experimental"

            produce = "library"
            moduleName = "co.touchlab.swiftgen:kotlin"

            this.temporaryFilesDir = tempDirectory.absolutePathString()
            outputName = outputFile.absolutePathString()
        }

    private fun interpretResult(
        result: ExitCode,
        klibFile: Path,
        outputStream: OutputStream,
    ): IntermediateResult<Path> = when (result) {
        ExitCode.OK -> {
            testResultBuilder.appendLog("Kotlin compiler", outputStream.toString())

            IntermediateResult.Value(klibFile)
        }

        else -> {
            val testResult = testResultBuilder.buildKotlinCompilationError(outputStream.toString())

            IntermediateResult.Error(testResult)
        }
    }
}
