package co.touchlab.skie.acceptancetests.framework.internal.testrunner.phases.kotlin

import co.touchlab.skie.acceptancetests.framework.TempFileSystem
import co.touchlab.skie.acceptancetests.framework.TestResult
import co.touchlab.skie.acceptancetests.framework.fromTestEnv
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.IntermediateResult
import co.touchlab.skie.acceptancetests.framework.internal.testrunner.TestLogger
import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.ConfigurationKeys
import co.touchlab.skie.plugin.SkieComponentRegistrar
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfiguration
import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
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
        val expandedSwiftDirectory = tempFileSystem.createDirectory("swiftpack-expanded")
        val analyticsDirectory = tempFileSystem.createDirectory("analytics")
        val skieBuildDirectory = tempFileSystem.createDirectory("skieBuild")

        PluginRegistrar.configure.set {
            put(ConfigurationKeys.SwiftCompiler.generatedDir, expandedSwiftDirectory.toFile())
            put(ConfigurationKeys.SwiftCompiler.parallelCompilation, false)
            add(ConfigurationKeys.SwiftCompiler.additionalFlags, "-verify-emitted-module-interface")
            put(ConfigurationKeys.Debug.infoDirectory, DebugInfoDirectory(tempFileSystem.createDirectory("skie-debug-info").toFile()))
            put(ConfigurationKeys.Debug.dumpSwiftApiPoints, DumpSwiftApiPoint.fromTestEnv())
            put(ConfigurationKeys.skieConfiguration, configuration + analyticsConfiguration)
            put(ConfigurationKeys.buildId, "tests-${UUID.randomUUID()}")
            put(ConfigurationKeys.jwtWithLicense, "foeman.aegis.lion.shirr.bide")
            put(ConfigurationKeys.analyticsDir, analyticsDirectory.toFile())
            put(ConfigurationKeys.skieBuildDir, skieBuildDirectory.toFile())
            put(ConfigurationKeys.disableWildcardExport, true)
        }

        PluginRegistrar.plugins.set(
            listOf {
                with(SkieComponentRegistrar()) {
                    registerExtensions(it)
                }
            },
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
