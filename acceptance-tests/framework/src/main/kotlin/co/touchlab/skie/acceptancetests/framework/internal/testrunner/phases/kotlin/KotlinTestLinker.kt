package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.configuration.features.SkieFeatureSet
import co.touchlab.skie.framework.BuildConfig
import co.touchlab.skie.plugin.ConfigurationKeys
import co.touchlab.skie.plugin.SkieComponentRegistrar
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfiguration
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.cli.bc.K2Native
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.copyTo

class KotlinTestLinker(
    private val tempFileSystem: TempFileSystem,
    private val testLogger: TestLogger,
) {

    private val analyticsConfiguration = Configuration(
        analyticsConfiguration = AnalyticsConfiguration.DISABLED,
    )

    fun link(klib: Path, configuration: Configuration?, compilerArgumentsProvider: CompilerArgumentsProvider): IntermediateResult<Path> {
        val tempDirectory = tempFileSystem.createDirectory("kotlin-linker")
        val outputFile = tempFileSystem.createDirectory("Kotlin.framework")

        configuration?.let { configureSwiftKt(configuration) }

        val (messageCollector, outputStream) = createCompilerOutputStream()

        val arguments = compilerArgumentsProvider.link(
            klib = klib,
            tempDirectory = tempDirectory,
            outputFile = outputFile,
        )

        val result = K2Native().exec(messageCollector, Services.EMPTY, arguments)

        return interpretResult(result, outputFile, outputStream)
    }

    private fun configureSwiftKt(configuration: Configuration) {
        val buildDirectory = tempFileSystem.createDirectory("skie")
        val skieDirectories = SkieDirectories(buildDirectory.toFile())

        skieDirectories.initializeForTests(configuration)

        PluginRegistrar.configure.set {
            put(ConfigurationKeys.buildId, "tests-${UUID.randomUUID()}")
            put(ConfigurationKeys.skieDirectories, skieDirectories)
            add(ConfigurationKeys.SwiftCompiler.additionalFlags, "-verify-emitted-module-interface")
        }

        PluginRegistrar.plugins.set(
            listOf {
                with(SkieComponentRegistrar()) {
                    registerExtensions(it)
                }
            },
        )
    }

    private fun SkieDirectories.initializeForTests(configuration: Configuration) {
        val mergedConfiguration = configuration + analyticsConfiguration + getDumpSwiftApiConfiguration()
        val serializedConfiguration = mergedConfiguration.serialize()
        buildDirectory.skieConfiguration.writeText(serializedConfiguration)

        BuildConfig.LICENSE_PATH.copyTo(buildDirectory.license.toPath())
    }

    private fun getDumpSwiftApiConfiguration(): Configuration =
        if (System.getenv("debugDumpSwiftApi") != null) {
            Configuration(enabledFeatures = SkieFeatureSet(SkieFeature.DumpSwiftApiBeforeApiNotes, SkieFeature.DumpSwiftApiAfterApiNotes))
        } else {
            Configuration()
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

    private fun interpretResult(
        result: ExitCode,
        outputFile: Path,
        outputStream: OutputStream,
    ): IntermediateResult<Path> {
        val output = outputStream.toString()
        testLogger.appendSection("Kotlin linker", output)

        return when (result) {
            ExitCode.OK -> IntermediateResult.Value(outputFile)
            else -> IntermediateResult.Error(TestResult.KotlinLinkingError(output))
        }
    }
}
