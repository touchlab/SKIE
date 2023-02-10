@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.test

import co.touchlab.skie.acceptancetests.framework.CompilerConfiguration
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
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
    private val testLogger: TestLogger,
) {

    fun compile(kotlinFiles: List<Path>, compilerConfiguration: CompilerConfiguration): IntermediateResult<Path> {
        val tempDirectory = tempFileSystem.createDirectory("kotlin-compiler")
        val outputFile = tempFileSystem.createFile("kotlin.klib")

        val (messageCollector, outputStream) = createCompilerOutputStream()

        val arguments = createCompilerArguments(kotlinFiles, tempDirectory, outputFile, compilerConfiguration)

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
        compilerConfiguration: CompilerConfiguration,
    ): K2NativeCompilerArguments =
        K2NativeCompilerArguments().apply {
            freeArgs = kotlinFiles.map { it.absolutePathString() }

            memoryModel = "experimental"

            produce = "library"
            moduleName = "co.touchlab.swiftgen:kotlin"

            this.temporaryFilesDir = tempDirectory.absolutePathString()
            outputName = outputFile.absolutePathString()

            libraries = compilerConfiguration.dependencies.toTypedArray()
        }

    private fun interpretResult(
        result: ExitCode,
        klibFile: Path,
        outputStream: OutputStream,
    ): IntermediateResult<Path> {
        val output = outputStream.toString()
        testLogger.appendSection("Kotlin compiler", output)
        return when (result) {
            ExitCode.OK -> IntermediateResult.Value(klibFile)
            else -> IntermediateResult.Error(TestResult.SwiftCompilationError(output))
        }
    }
}
