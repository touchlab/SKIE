package co.touchlab.swiftgen.irinspector

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.irinspector.plugin.IrInspectorPluginCommandLineProcessor
import co.touchlab.swiftgen.irinspector.plugin.IrInspectorPluginOption
import org.jetbrains.kotlin.cli.bc.K2Native
import org.jetbrains.kotlin.cli.bc.K2NativeCompilerArguments
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.config.Services
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.readText

private val defaultInputPath = Path(BuildConfig.CODE)

fun main(args: Array<String>) {
    val inputPath = if (args.isEmpty()) defaultInputPath else Path(args.first())

    runInspector(inputPath, System.out)
}

fun runInspector(inputPath: Path, outputStream: OutputStream) {
    val inputFiles = getInputFiles(inputPath)
    val outputFile = createTempFile()

    val arguments = createCompilerArguments(inputFiles, outputFile)

    runCompiler(arguments)

    printResult(outputStream, outputFile.readText())
}

private fun runCompiler(
    arguments: K2NativeCompilerArguments,
) {
    val messageCollector = PrintingMessageCollector(
        PrintStream(System.err),
        MessageRenderer.GRADLE_STYLE,
        false,
    )

    val result = K2Native().exec(messageCollector, Services.EMPTY, arguments)

    if (result != ExitCode.OK) {
        throw IllegalStateException("Compilation failed.")
    }
}

private fun getInputFiles(inputPath: Path): List<Path> {
    val inputFiles = inputPath.toFile().walk().filter { it.isFile }.map { it.toPath() }.toList()

    if (inputFiles.isEmpty()) {
        throw IllegalArgumentException("No files to compile.")
    }

    return inputFiles
}

private fun createCompilerArguments(
    files: List<Path>,
    output: Path,
): K2NativeCompilerArguments =
    K2NativeCompilerArguments().apply {
        freeArgs = files.map { it.absolutePathString() }

        pluginClasspaths = (pluginClasspaths ?: emptyArray()) + arrayOf(BuildConfig.RESOURCES)
        pluginOptions = (pluginOptions ?: emptyArray()) + createConfigurationPathOption(output)

        memoryModel = "experimental"
        produce = "library"
        verbose = false
        suppressWarnings = true

        temporaryFilesDir = createTempDirectory().absolutePathString()
        outputName = createTempFile().absolutePathString()
    }

private fun createConfigurationPathOption(output: Path): String =
    "plugin:${IrInspectorPluginCommandLineProcessor.pluginId}:${IrInspectorPluginOption.Output.optionName}=${output.absolutePathString()}"

private fun printResult(outputStream: OutputStream, irDump: String) {
    outputStream.bufferedWriter().let {
        it.write(irDump)
        it.flush()
    }
}
