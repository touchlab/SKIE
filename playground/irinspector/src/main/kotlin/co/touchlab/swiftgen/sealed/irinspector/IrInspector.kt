package co.touchlab.swiftgen.sealed.irinspector

import co.touchlab.swiftgen.playground.BuildConfig
import co.touchlab.swiftgen.sealed.irinspector.plugin.IrInspectorPluginCommandLineProcessor
import co.touchlab.swiftgen.sealed.irinspector.plugin.IrInspectorPluginComponentRegistrar
import co.touchlab.swiftgen.sealed.irinspector.plugin.IrInspectorPluginOption
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.readText

private val defaultInputPath = Path(BuildConfig.RESOURCES)

fun main(args: Array<String>) {
    val inputPath = if (args.isEmpty()) defaultInputPath else Path(args.first())

    runInspector(inputPath, System.out)
}

fun runInspector(inputPath: Path, outputStream: OutputStream) {
    val compiler = KotlinCompilation()

    val inputFiles = getInputFiles(inputPath)
    val outputFile = createTempFile()

    configureCompiler(compiler, inputFiles, outputFile)

    val irDump = inspectIr(compiler, outputFile)

    printResult(outputStream, irDump)
}

private fun getInputFiles(inputPath: Path): List<Path> {
    val inputFiles = inputPath.toFile().walk().filter { it.isFile }.map { it.toPath() }.toList()

    if (inputFiles.isEmpty()) {
        throw IllegalArgumentException("No files to compile.")
    }

    return inputFiles
}

private fun configureCompiler(compiler: KotlinCompilation, files: List<Path>, output: Path) {
    compiler.apply {
        sources = files.map { SourceFile.fromPath(it.toFile()) }

        compilerPlugins = listOf<ComponentRegistrar>(
            IrInspectorPluginComponentRegistrar()
        )

        commandLineProcessors = listOf<CommandLineProcessor>(
            IrInspectorPluginCommandLineProcessor()
        )

        pluginOptions = listOf(
            PluginOption(
                IrInspectorPluginCommandLineProcessor.pluginId,
                IrInspectorPluginOption.Output.optionName,
                output.absolutePathString(),
            )
        )

        useIR = true
        verbose = false
        suppressWarnings = true
    }
}

private fun inspectIr(compiler: KotlinCompilation, outputFile: Path): String {
    val result = compiler.compile()

    if (result.exitCode != KotlinCompilation.ExitCode.OK) {
        throw IllegalArgumentException(result.messages)
    }

    return outputFile.readText()
}

private fun printResult(outputStream: OutputStream, irDump: String) {
    outputStream.bufferedWriter().let {
        it.write(irDump)
        it.flush()
    }
}
