package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.BuildConfig
import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner.TestResultBuilder
import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftlink.plugin.ConfigurationKeys as SwiftLinkConfigurationKeys
import co.touchlab.swiftgen.plugin.ConfigurationKeys as SwiftGenConfigurationKeys
import co.touchlab.swiftlink.plugin.SwiftLinkComponentRegistrar
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import co.touchlab.swiftpack.spec.module.SwiftPackModule
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
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText

internal class KotlinTestLinker(
    private val tempFileSystem: TempFileSystem,
    private val testResultBuilder: TestResultBuilder,
) {

    fun link(klib: Path, configuration: Path): IntermediateResult<Path> {
        val tempDirectory = tempFileSystem.createDirectory("kotlin-linker")
        val outputFile = tempFileSystem.createDirectory("Kotlin.framework")

        val generatedSwiftDirectory = configureSwiftKt(configuration)

        val (messageCollector, outputStream) = createCompilerOutputStream()

        val arguments = createCompilerArguments(klib, tempDirectory, outputFile)

        val result = K2Native().exec(messageCollector, Services.EMPTY, arguments)

        return interpretResult(result, outputFile, outputStream, generatedSwiftDirectory)
    }

    private fun configureSwiftKt(configuration: Path): Path {
        val outputDirectory = tempFileSystem.createDirectory("swiftpack")
        val expandedSwiftDirectory = tempFileSystem.createDirectory("swiftpack-expanded")

        SwiftPackModuleBuilder.Config.outputDir = outputDirectory.toFile()

        PluginRegistrar.configure.set {
            val swiftPackModule = SwiftPackModule.Reference("Kotlin", outputDirectory.toFile())

            add(SwiftLinkConfigurationKeys.swiftPackModules, swiftPackModule)
            put(SwiftLinkConfigurationKeys.expandedSwiftDir, expandedSwiftDirectory.toFile())
            put(SwiftGenConfigurationKeys.swiftGenConfiguration, Configuration.deserialize(configuration.readText()))
        }

        PluginRegistrar.plugins.set(
            listOf(
                SwiftLinkComponentRegistrar(),
            )
        )

        return expandedSwiftDirectory
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
        }

    private fun interpretResult(
        result: ExitCode,
        outputFile: Path,
        outputStream: OutputStream,
        generatedSwiftDirectory: Path,
    ): IntermediateResult<Path> {
        val generatedSwift = generatedSwiftDirectory.listDirectoryEntries().joinToString("\n") {
            "------ ${it.name} ------\n" + it.readText()
        }
        testResultBuilder.appendLog("Generated Swift", generatedSwift)

        return when (result) {
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
}
