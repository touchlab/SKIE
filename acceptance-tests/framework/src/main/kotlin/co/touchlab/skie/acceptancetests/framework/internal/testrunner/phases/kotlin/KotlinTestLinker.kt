package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.acceptancetests.framework.CompilerConfiguration
import co.touchlab.skie.framework.BuildConfig
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestResultBuilder
import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.SwiftLinkComponentRegistrar
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
import kotlin.io.path.readText
import co.touchlab.skie.plugin.ConfigurationKeys as SwiftLinkConfigurationKeys
import co.touchlab.skie.plugin.generator.ConfigurationKeys as SwiftGenConfigurationKeys

internal class KotlinTestLinker(
    private val tempFileSystem: TempFileSystem,
    private val testResultBuilder: TestResultBuilder,
) {

    fun link(klib: Path, configuration: Path, compilerConfiguration: CompilerConfiguration): IntermediateResult<Path> {
        val tempDirectory = tempFileSystem.createDirectory("kotlin-linker")
        val outputFile = tempFileSystem.createDirectory("Kotlin.framework")

        configureSwiftKt(configuration)

        val (messageCollector, outputStream) = createCompilerOutputStream()

        val arguments = createCompilerArguments(klib, tempDirectory, outputFile, compilerConfiguration)

        val result = K2Native().exec(messageCollector, Services.EMPTY, arguments)

        return interpretResult(result, outputFile, outputStream)
    }

    private fun configureSwiftKt(configuration: Path) {
        val expandedSwiftDirectory = tempFileSystem.createDirectory("swiftpack-expanded")

        PluginRegistrar.configure.set {
            put(SwiftLinkConfigurationKeys.generatedSwiftDir, expandedSwiftDirectory.toFile())
            put(SwiftGenConfigurationKeys.swiftGenConfiguration, Configuration.deserialize(configuration.readText()))
        }

        PluginRegistrar.plugins.set(
            listOf(
                SwiftLinkComponentRegistrar(),
            )
        )
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
        klib: Path,
        tempDirectory: Path,
        outputFile: Path,
        compilerConfiguration: CompilerConfiguration,
    ): K2NativeCompilerArguments =
        K2NativeCompilerArguments().apply {
            includes = (includes ?: emptyArray()) + klib.absolutePathString()

            memoryModel = "experimental"

            produce = "framework"
            staticFramework = true

            temporaryFilesDir = tempDirectory.absolutePathString()
            outputName = outputFile.absolutePathString()
            bundleId = "Kotlin"

            pluginClasspaths = (pluginClasspaths ?: emptyArray()) + arrayOf(BuildConfig.RESOURCES)

            libraries = compilerConfiguration.dependencies.toTypedArray()
            exportedLibraries = compilerConfiguration.exportedDependencies.toTypedArray()
        }

    private fun interpretResult(
        result: ExitCode,
        outputFile: Path,
        outputStream: OutputStream,
    ): IntermediateResult<Path> = when (result) {
        ExitCode.OK -> {
            testResultBuilder.appendLog("Kotlin linker", outputStream.toString())

            IntermediateResult.Value(outputFile)
        }
        else -> {
            val testResult = testResultBuilder.buildKotlinLinkingError(outputStream.toString())

            IntermediateResult.Error(testResult)
        }
    }
}
