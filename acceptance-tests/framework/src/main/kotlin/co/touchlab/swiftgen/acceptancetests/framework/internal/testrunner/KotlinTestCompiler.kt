package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestResult
import org.jetbrains.kotlin.cli.bc.K2Native
import org.jetbrains.kotlin.cli.bc.K2NativeCompilerArguments
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class KotlinTestCompiler(private val tempFileSystem: TempFileSystem, private val logger: Logger) {

    fun compile(kotlinFiles: List<Path>): IntermediateResult<Path> {
        val outputDirectory = tempFileSystem.createDirectory()

        val arguments = K2NativeCompilerArguments().apply {
            freeArgs = kotlinFiles.map { it.absolutePathString() }

            produce = "framework"
            staticFramework = true

            temporaryFilesDir = outputDirectory.absolutePathString()
            outputName = outputDirectory.resolve("Kotlin").absolutePathString()
            bundleId = "Kotlin"

            // TODO plugins
        }

        val compiler = K2Native()

        val outputStream = ByteArrayOutputStream()
        val internalMessageStream = PrintStream(outputStream)

        val compilerMessageCollector = PrintingMessageCollector(
            internalMessageStream, MessageRenderer.GRADLE_STYLE, false
        )

        val result = compiler.exec(compilerMessageCollector, Services.EMPTY, arguments)

        return when (result) {
            ExitCode.OK -> {
                logger.write("Kotlin compilation", outputStream.toString())

                IntermediateResult.Value(outputDirectory)
            }
            else -> {
                val testResult = TestResult.KotlinCompilationError(logger.toString(), outputStream.toString())

                IntermediateResult.Error(testResult)
            }
        }
    }
}