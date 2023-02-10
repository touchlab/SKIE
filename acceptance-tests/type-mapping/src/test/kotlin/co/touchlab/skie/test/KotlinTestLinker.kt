@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.test

import co.touchlab.skie.acceptancetests.framework.CompilerConfiguration
import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin.PluginRegistrar
import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.ConfigurationKeys
import co.touchlab.skie.plugin.SkieComponentRegistrar
import co.touchlab.skie.type_mapping.BuildConfig
import org.jetbrains.kotlin.cli.bc.K2Native
import org.jetbrains.kotlin.cli.common.arguments.K2NativeCompilerArguments
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString

internal class KotlinTestLinker(
    private val tempFileSystem: TempFileSystem,
    private val testLogger: TestLogger,
) {

    fun link(klib: Path, configuration: Configuration, compilerConfiguration: CompilerConfiguration): TestResult {
        val tempDirectory = tempFileSystem.createDirectory("kotlin-linker")
        val outputFile = tempFileSystem.createDirectory("Kotlin.framework")

        configureSwiftKt(configuration)

        val (messageCollector, outputStream) = createCompilerOutputStream()

        val arguments = createCompilerArguments(klib, tempDirectory, outputFile, compilerConfiguration)

        val result = K2Native().exec(messageCollector, Services.EMPTY, arguments)

        return interpretResult(result, outputFile, outputStream)
    }

    private fun configureSwiftKt(configuration: Configuration) {
        val expandedSwiftDirectory = tempFileSystem.createDirectory("swiftpack-expanded")

        PluginRegistrar.configure.set {
            put(ConfigurationKeys.generatedSwiftDir, expandedSwiftDirectory.toFile())
            put(co.touchlab.skie.plugin.generator.ConfigurationKeys.swiftGenConfiguration, configuration)
        }

        PluginRegistrar.plugins.set(
            listOf {
                with (SkieComponentRegistrar()) {
                    registerExtensions(it)
                }
            }
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

            disablePhases = arrayOf(
                "Linker",
                "BitcodeOptimization",
                "FinalizeDebugInfo",
                "VerifyBitcode",
                "LinkBitcodeDependencies",
                "BitcodePostprocessing",
            )
            pluginClasspaths = (pluginClasspaths ?: emptyArray()) + arrayOf(BuildConfig.TEST_RESOURCES)

            libraries = compilerConfiguration.dependencies.toTypedArray()
            exportedLibraries = compilerConfiguration.exportedDependencies.toTypedArray()
        }

    private fun interpretResult(
        result: ExitCode,
        outputFile: Path,
        outputStream: OutputStream,
    ): TestResult {
        val output = outputStream.toString()
        testLogger.appendSection("Kotlin linker", output)
        return when (result) {
            ExitCode.OK -> TestResult.Success
            else -> TestResult.KotlinLinkingError(output)
        }
    }
}
