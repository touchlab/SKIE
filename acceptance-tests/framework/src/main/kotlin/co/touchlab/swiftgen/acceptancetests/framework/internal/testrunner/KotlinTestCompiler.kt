package co.touchlab.swiftgen.acceptancetests.framework.internal.testrunner

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftgen.acceptancetests.framework.TempFileSystem
import co.touchlab.swiftgen.acceptancetests.framework.internal.TestResult
import co.touchlab.swiftgen.plugin.SwiftGenComponentRegistrar
import co.touchlab.swiftkt.plugin.ConfigurationKeys
import co.touchlab.swiftkt.plugin.SwiftKtComponentRegistrar
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
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

internal class KotlinTestCompiler(private val tempFileSystem: TempFileSystem, private val logger: Logger) {

    fun compile(kotlinFiles: List<Path>): IntermediateResult<Path> {
        val outputDirectory = tempFileSystem.createDirectory()

        configureSwiftKt()

        val (messageCollector, outputStream) = createCompilerOutputStream()

        val arguments = createCompilerArguments(kotlinFiles, outputDirectory)

        val result = K2Native().exec(messageCollector, Services.EMPTY, arguments)

        return interpretResult(result, outputDirectory, outputStream)
    }

    private fun configureSwiftKt() {
        val outputDir = tempFileSystem.createDirectory().toFile()

        SwiftPackModuleBuilder.Config.outputDir = outputDir

        PluginRegistrar.configure.set {
            add(ConfigurationKeys.swiftPackModules, NamespacedSwiftPackModule.Reference("Kotlin", outputDir))
            put(ConfigurationKeys.expandedSwiftDir, tempFileSystem.createDirectory().toFile())
        }

        PluginRegistrar.plugins.set(
            listOf(
                SwiftKtComponentRegistrar(),
                SwiftGenComponentRegistrar(),
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

    private fun createCompilerArguments(kotlinFiles: List<Path>, outputDirectory: Path): K2NativeCompilerArguments =
        K2NativeCompilerArguments().apply {
            freeArgs = kotlinFiles.map { it.absolutePathString() }

            produce = "framework"
            staticFramework = true

            temporaryFilesDir = outputDirectory.absolutePathString()
            outputName = outputDirectory.resolve("Kotlin").absolutePathString()
            bundleId = "Kotlin"

            pluginClasspaths = (pluginClasspaths ?: emptyArray()) + arrayOf(BuildConfig.RESOURCES)
        }

    private fun interpretResult(
        result: ExitCode,
        outputDirectory: Path,
        outputStream: OutputStream,
    ): IntermediateResult<Path> = when (result) {
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